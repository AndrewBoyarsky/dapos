package com.boyarsky.dapos.core;

import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.StoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class TransactionManager {
    private final PersistentEntityStore store;
    private final AtomicReference<StoreTransaction> txn = new AtomicReference<>();
    @Autowired
    public TransactionManager(PersistentEntityStore store) {
        this.store = store;
    }

    public void begin() {

        if (currentTx() != null) {
            currentTx().abort();
        }
        txn.set(store.beginTransaction());
        log.info("Begin accepting application state changes");
    }

    public void rollback() {
        requireBeganTx().abort();
        clearTx();
    }

    public void commit() {
        boolean commit = requireBeganTx().commit();
        clearTx();
        if (!commit) {
            throw new RuntimeException("Unable to commit changes for " + txn);
        }
        log.info("Commit application state");

    }

    public StoreTransaction currentTx() {
        return txn.get();
    }

    private StoreTransaction requireBeganTx() {
        if (txn.get() == null) {
            throw new RuntimeException("Transaction should be begun");
        }
        return txn.get();
    }

    private void clearTx() {
        txn.set(null);
    }

}
