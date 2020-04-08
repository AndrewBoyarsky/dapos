package com.boyarsky.dapos.core;

import com.boyarsky.dapos.core.dao.BlockchainDao;
import com.boyarsky.dapos.core.dao.model.LastSuccessBlockData;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.env.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class Blockchain {
    private volatile long currentHeight;
    private BlockchainDao blockchainDao;

    @Autowired
    public Blockchain(BlockchainDao blockchainDao) {
        this.blockchainDao = blockchainDao;
    }

    public long getCurrentBlockHeight() {
        return currentHeight;
    }

    public void beginBlock(long height) {
        this.currentHeight = height;
    }

    public void addNewBlock(byte[] hash, StoreTransaction txn) {
        blockchainDao.insert(new LastSuccessBlockData(hash, currentHeight), txn);
    }

    public LastSuccessBlockData getLastBlock() {
        return blockchainDao.getLastBlock();
    }
}
