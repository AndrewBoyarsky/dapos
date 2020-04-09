package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class FeeProviderRepository {
    public static final String storeName = "blockchain";
    private Environment env;

    @Autowired
    public FeeProviderRepository(Environment env) {
        this.env = env;
    }

    public void getById(long id) {
//        return env.computeInReadonlyTransaction(txn -> {
//            Store store = env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
//            ByteIterable result = store.get(txn, new ByteBufferByteIterable(ByteBuffer.wrap(lastBlockDataKey.getBytes())));
//            if (result != null) {
//                return LastSuccessBlockData.fromBytes(result.getBytesUnsafe());
//            }
//            return null;
//        });
    }

    public void insert(LastSuccessBlockData blockData, Transaction txn) {
        Store store = env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
//        store.put(txn, new ByteBufferByteIterable(ByteBuffer.wrap(lastBlockDataKey.getBytes())), new ByteBufferByteIterable(ByteBuffer.wrap(blockData.toBytes())));
    }
}
