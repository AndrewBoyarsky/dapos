package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
    private final AccountService service;
    private final LedgerService ledgerService;
    private volatile byte[] proposerAddress;
    private AtomicLong rewardAmount = new AtomicLong();

    @Autowired
    public Blockchain(BlockRepository blockRepository, BlockchainConfig config, Genesis genesis, TransactionProcessor processor, TransactionManager manager, AccountService service, LedgerService ledgerService) {
        this.blockRepository = blockRepository;
        this.config = config;
        this.genesis = genesis;
        this.processor = processor;
        this.manager = manager;
        this.service = service;
        this.ledgerService = ledgerService;
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

    public void beginBlock(long height, byte[] proposerAddress) {
        this.currentHeight = height;
        rewardAmount.addAndGet(config.getCurrentConfig().getBlockReward());
        this.proposerAddress = proposerAddress;
        manager.begin();
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
        rewardAmount.set(0);
        return hash;
    }

    public HeightConfig endBlock() {
        AccountId validatorAddress = new AccountId(CryptoUtils.encodeValidatorAddress(proposerAddress));
        Account account = service.get(validatorAddress);
        account.setBalance(account.getBalance() + rewardAmount.get());
        account.setHeight(currentHeight);
        service.save(account);
        ledgerService.add(new LedgerRecord(currentHeight, rewardAmount.get(), LedgerRecord.Type.BLOCK_REWARD, null, validatorAddress));
        boolean updated = config.tryUpdateForHeight(getCurrentBlockHeight() + 1);
        if (updated) {
            HeightConfig currentConfig = config.getCurrentConfig();
            log.info("Update config to: " + currentConfig);
            return currentConfig;
        } else {
            return null;
        }
    }
}
