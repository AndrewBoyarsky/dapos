package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

class BlockchainTest {

    private final AtomicLong rewardAmount = new AtomicLong();
    Blockchain blockchain;
    BlockchainConfig config;
    BlockRepository blockRepository;
    Genesis genesis;
    TransactionProcessor processor;
    TransactionManager manager;
    ValidatorService validatorService;

    @Test
    void getCurrentBlockHeight() {

    }

    @Test
    void beginBlock() {
    }

    @Test
    void addNewBlock() {
    }

    @Test
    void getLastBlock() {
    }

    @Test
    void deliverTx() {
    }

    @Test
    void checkTx() {
    }

    @Test
    void onInitChain() {
    }

    @Test
    void commitBlock() {
    }

    @Test
    void endBlock() {
    }
}