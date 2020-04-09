package com.boyarsky.dapos.core;

import com.boyarsky.dapos.core.repository.BlockchainRepository;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Blockchain {
    private volatile long currentHeight;
    private BlockchainRepository blockchainRepository;

    @Autowired
    public Blockchain(BlockchainRepository blockchainRepository) {
        this.blockchainRepository = blockchainRepository;
    }

    public long getCurrentBlockHeight() {
        return currentHeight;
    }

    public void beginBlock(long height) {
        this.currentHeight = height;
    }

    public void addNewBlock(byte[] hash, StoreTransaction txn) {
        blockchainRepository.insert(new LastSuccessBlockData(hash, currentHeight), txn);
    }

    public LastSuccessBlockData getLastBlock() {
        return blockchainRepository.getLastBlock();
    }
}
