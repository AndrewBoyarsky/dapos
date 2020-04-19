package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockchainRepository {
    private static final String storeName = "lastBlock";
    private XodusRepoContext context;

    @Autowired
    public BlockchainRepository(XodusRepoContext context) {
        this.context = context;
    }

    @Transactional(readonly = true)
    public LastSuccessBlockData getLastBlock() {
        return getLastBlock(context.getTx());
    }

    public LastSuccessBlockData getLastBlock(StoreTransaction txn) {
        EntityIterable all = txn.getAll(storeName);
        long size = all.size();
        if (size == 0) {
            return null;
        } else if (size > 1) {
            throw new RuntimeException("More than 1 last block detected: " + size);
        }
        Entity first = all.getFirst();
        String hash = (String) first.getProperty("hash");
        long height = (Long) first.getProperty("height");
        return new LastSuccessBlockData(Convert.parseHexString(hash), height);
    }

    public void insert(LastSuccessBlockData blockData) {
        StoreTransaction txn = context.getTx();
        Entity prev = txn.getAll(storeName).getFirst();
        if (prev == null) {
            prev = txn.newEntity(storeName);
        }
        prev.setProperty("hash", Convert.toHexString(blockData.getAppHash()));
        prev.setProperty("height", blockData.getHeight());
    }
}
