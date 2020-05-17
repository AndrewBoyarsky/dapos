package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
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
    private final AccountService service;
    private final LedgerService ledgerService;
    private final ValidatorService validatorService;

    private AtomicLong rewardAmount = new AtomicLong();

    @Autowired
    public Blockchain(BlockRepository blockRepository, BlockchainConfig config, Genesis genesis, TransactionProcessor processor, TransactionManager manager, AccountService service, LedgerService ledgerService, ValidatorService validatorService) {
        this.blockRepository = blockRepository;
        this.config = config;
        this.genesis = genesis;
        this.processor = processor;
        this.manager = manager;
        this.service = service;
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
        byzantineValidators.forEach(validatorService::punishByzantine);
        validatorStatuses.entrySet().stream().filter(e -> !e.getValue()).map(Map.Entry::getKey).forEach(validatorService::punishAbsent);
        long maxValidators = config.maxValidatorsForHeight(this.currentHeight);
        List<ValidatorEntity> fairValidators = validatorStatuses.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .map(validatorService::get)
                .filter(ValidatorEntity::isEnabled)
                .sorted(Comparator.comparing(ValidatorEntity::getDelegatedBalance).reversed())
                .limit(maxValidators)
                .collect(Collectors.toList());
        validatorService.distributeReward(fairValidators, rewardAmount.get());


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

    public HeightConfig onInitChain() {
        manager.begin();
        try {
            log.info("Applying genesis");
            genesis.initialize();
            manager.commit();
        } catch (Exception e) {
            log.error("Genesis init error", e);
            manager.rollback();
        }
        log.info("Genesis applied");
        config.init(1);
        return config.getCurrentConfig();
    }

    public byte[] commitBlock() {
        byte[] hash = new byte[8];
        addNewBlock(hash);
        manager.commit();
        return hash;
    }

    public EndBlockEnvelope endBlock() {
//        AccountId validatorAddress = new AccountId(CryptoUtils.encodeValidatorAddress(proposerAddress));
//        Account account = service.get(validatorAddress);
//        account.setBalance(account.getBalance() + rewardAmount.get());
//        account.setHeight(currentHeight);
//        service.save(account);
//        ledgerService.add(new LedgerRecord(currentHeight, rewardAmount.get(), LedgerRecord.Type.BLOCK_REWARD, null, validatorAddress));
//        validatorService.
//        int validatorSize = config.getCurrentConfig().getMaxValidators();

        boolean updated = config.tryUpdateForHeight(getCurrentBlockHeight() + 1);
        HeightConfig currentConfig = null;
        if (updated) {
            currentConfig = config.getCurrentConfig();
            log.info("Update config to: " + currentConfig);
        }
        EndBlockEnvelope response = new EndBlockEnvelope();
        response.setNewConfig(currentConfig);

        response.setValidators(null);
        return response;
    }
}
