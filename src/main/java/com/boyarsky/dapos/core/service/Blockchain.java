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
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.Evidence;
import types.VoteInfo;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Blockchain {
    private volatile long currentHeight;
    private final BlockchainConfig config;
    private final BlockRepository blockRepository;
    private final Genesis genesis;
    private final TransactionProcessor processor;
    private final TransactionManager manager;
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final ValidatorService validatorService;

    private AtomicLong rewardAmount = new AtomicLong();

    @Autowired
    public Blockchain(BlockRepository blockRepository, BlockchainConfig config, Genesis genesis, TransactionProcessor processor, TransactionManager manager, AccountService accountService, LedgerService ledgerService, ValidatorService validatorService) {
        this.blockRepository = blockRepository;
        this.config = config;
        this.genesis = genesis;
        this.processor = processor;
        this.manager = manager;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
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

        Set<AccountId> byzantineValidators = new HashSet<>();
        for (Evidence evidence : byzantineValidatorsList) {
            byzantineValidators.add(new AccountId(CryptoUtils.encodeValidatorAddress(evidence.getValidator().getAddress().toByteArray())));
        }
        Map<AccountId, Boolean> validatorStatuses = new HashMap<>();
        for (VoteInfo voteInfo : votesList) {
            byte[] addressBytes = voteInfo.getValidator().getAddress().toByteArray();
            AccountId validatorId = new AccountId(CryptoUtils.encodeValidatorAddress(addressBytes));
            validatorStatuses.put(validatorId, voteInfo.getSignedLastBlock());
        }
        byzantineValidators.forEach(validatorStatuses::remove); // avoid double punishment
        long totalPunishmentAmount = byzantineValidators
                .stream()
                .mapToLong(id -> validatorService.punishByzantine(id, currentHeight))
                .sum();
        totalPunishmentAmount += validatorStatuses.entrySet()
                .stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .mapToLong(id -> validatorService.punishAbsent(id, currentHeight))
                .sum();
        long maxValidators = config.maxValidatorsForHeight(this.currentHeight);
        List<ValidatorEntity> fairValidators = validatorStatuses.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .map(id -> {
                    ValidatorEntity validator = validatorService.get(id);
                    return Objects.requireNonNullElseGet(validator, ValidatorEntity::new);
                })
                .filter(ValidatorEntity::isEnabled)
                .sorted(Comparator.comparing(ValidatorEntity::getVotePower).reversed())
                .limit(maxValidators)
                .collect(Collectors.toList());
        rewardAmount.addAndGet(totalPunishmentAmount);
        validatorService.distributeReward(fairValidators, rewardAmount.get(), 0);


        this.currentHeight = height;
        rewardAmount.set(0);
        rewardAmount.addAndGet(config.getCurrentConfig().getBlockReward());
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
        config.init(1);
        InitChainResponse initChainResponse = new InitChainResponse();
        initChainResponse.setConfig(config.getCurrentConfig());
        initChainResponse.setGenesisInitResponse(genesisInitResponse);
        return initChainResponse;
    }

    public byte[] commitBlock() {
        byte[] hash = new byte[8];
        addNewBlock(hash);
        manager.commit();
        return hash;
    }

    public EndBlockEnvelope endBlock() {
        boolean updated = config.tryUpdateForHeight(getCurrentBlockHeight() + 1);
        HeightConfig currentConfig = null;
        if (updated) {
            currentConfig = config.getCurrentConfig();
            log.info("Update config to: " + currentConfig);
        }
        EndBlockEnvelope response = new EndBlockEnvelope();
        response.setNewConfig(currentConfig);
        if (currentHeight - 1 > 0) {
            List<ValidatorEntity> allUpdated = validatorService.getAllUpdated(currentHeight - 1);
            response.setValidators(allUpdated);
        }
        return response;
    }
}
