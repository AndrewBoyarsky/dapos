package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.genesis.GenesisInitResponse;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.Evidence;
import types.VoteInfo;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class Blockchain {
    private volatile long currentHeight;
    private final BlockchainConfig config;
    private final BlockRepository blockRepository;
    private final Genesis genesis;
    private final TransactionProcessor processor;
    private final TransactionManager manager;
    private final ValidatorService validatorService;

    private final AtomicLong rewardAmount = new AtomicLong();

    @Autowired
    public Blockchain(BlockRepository blockRepository, BlockchainConfig config, Genesis genesis, TransactionProcessor processor, TransactionManager manager, ValidatorService validatorService) {
        this.blockRepository = blockRepository;
        this.config = config;
        this.genesis = genesis;
        this.processor = processor;
        this.manager = manager;
        this.validatorService = validatorService;
    }

    @PostConstruct
    void init() {
        if (getLastBlock() != null) {
            config.init(getLastBlock().getHeight());
        }
    }

    public long getCurrentBlockHeight() {
        return currentHeight;
    }

    public void beginBlock(long height, List<VoteInfo> votesList, List<Evidence> byzantineValidatorsList) {
        manager.begin();
        this.currentHeight = height;

        Set<AccountId> byzantineValidators = new HashSet<>();
        for (Evidence evidence : byzantineValidatorsList) {
            byzantineValidators.add(new AccountId(CryptoUtils.encodeValidatorAddress(evidence.getValidator().getAddress().toByteArray())));
        }
        Set<AccountId> conscientiousValidators = new HashSet<>();
        Set<AccountId> absentValidators = new HashSet<>();
        for (VoteInfo voteInfo : votesList) {
            byte[] addressBytes = voteInfo.getValidator().getAddress().toByteArray();
            AccountId validatorId = new AccountId(CryptoUtils.encodeValidatorAddress(addressBytes));
            if (byzantineValidators.contains(validatorId)) {
                continue; // avoid double punishment
            }
            if (voteInfo.getSignedLastBlock()) {
                conscientiousValidators.add(validatorId);
            } else {
                absentValidators.add(validatorId);
            }
        }
        long totalPunishmentAmount = validatorService.punishByzantines(byzantineValidators, currentHeight);
        totalPunishmentAmount += validatorService.punishAbsents(absentValidators, currentHeight);
        rewardAmount.addAndGet(totalPunishmentAmount);
        validatorService.distributeReward(conscientiousValidators, rewardAmount.get(), currentHeight);

        rewardAmount.set(0);
        rewardAmount.addAndGet(config.getBlockReward());
    }

    public void addNewBlock(byte[] hash) {
        blockRepository.insert(new LastSuccessBlockData(hash, currentHeight));
    }

    public LastSuccessBlockData getLastBlock() {
        return blockRepository.getLastBlock();
    }

    public ProcessingResult deliverTx(byte[] tx) {
        ProcessingResult processingResult = processor.tryDeliver(tx, currentHeight);
        if (processingResult.getCode().isOk()) {
            rewardAmount.addAndGet(processingResult.getTx().getFee());
        }
        return processingResult;
    }

    public ProcessingResult checkTx(byte[] tx) {
        return processor.parseAndValidate(tx);
    }

    @Transactional(startNew = true)
    public InitChainResponse onInitChain() {
        log.info("Applying genesis");
        GenesisInitResponse genesisInitResponse = genesis.initialize();
        log.info("Genesis applied, accounts: {}, validators: {}", genesisInitResponse.getNumberOfAccount(), genesisInitResponse.getValidatorEntities().size());
        HeightConfig initConfig = config.init(1);
        InitChainResponse initChainResponse = new InitChainResponse();
        initChainResponse.setConfig(initConfig);
        initChainResponse.setGenesisInitResponse(genesisInitResponse);
        return initChainResponse;
    }

    public byte[] commitBlock() {
        byte[] hash = new byte[8];
        addNewBlock(hash);
        manager.commit();
        return hash;
    }

    public EndBlockResponse endBlock() {
        HeightConfig newConfig = config.tryUpdateForHeight(getCurrentBlockHeight() + 1);
        if (newConfig != null) {
            log.info("Update config to: " + newConfig);
        }
        EndBlockResponse response = new EndBlockResponse();
        response.setNewConfig(newConfig);
        if (currentHeight - 1 > 0) {
            List<ValidatorEntity> allUpdated = validatorService.getAllUpdated(currentHeight - 1);
            response.setValidators(allUpdated);
        }
        return response;
    }
}
