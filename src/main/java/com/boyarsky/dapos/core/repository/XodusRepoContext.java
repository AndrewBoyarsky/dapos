package com.boyarsky.dapos.core.repository;

import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.StoreTransaction;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class XodusRepoContext {
    final PersistentEntityStore store;

    @Autowired
    public XodusRepoContext(PersistentEntityStore store) {
        this.store = store;
    }

    public StoreTransaction getTx() {
        StoreTransaction storeTransaction = curTx();
        if (storeTransaction == null) {
            throw new IllegalStateException("Tx was not started");
        }
        return storeTransaction;
    }

    private StoreTransaction curTx() {
        if (store.getCurrentTransaction() != null) {
            return store.getCurrentTransaction();
        }
        return null;
    }

    public boolean inTx() {
        return curTx() != null;
    }
}
