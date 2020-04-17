package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.TransactionManager;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.StoreTransaction;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class XodusRepoContext {
    final PersistentEntityStore store;
    final TransactionManager manager;

    @Autowired
    public XodusRepoContext(PersistentEntityStore store, TransactionManager manager) {
        this.store = store;
        this.manager = manager;
    }

    public StoreTransaction getTx() {
        StoreTransaction storeTransaction = curTx();
        if (storeTransaction == null) {
            throw new IllegalStateException("Tx was not started");
        }
        return storeTransaction;
    }

    private StoreTransaction curTx() {
        if (manager.currentTx() != null) {
            return manager.currentTx();
        }
        if (store.getCurrentTransaction() != null) {
            return store.getCurrentTransaction();
        }
        return null;
    }

    public boolean inTx() {
        return curTx() != null;
    }
}
