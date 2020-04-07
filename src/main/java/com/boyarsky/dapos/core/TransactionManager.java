package com.boyarsky.dapos.core;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionManager {
    private final Environment environment;
    private Transaction txn;
    @Autowired
    public TransactionManager(Environment environment) {
        this.environment = environment;
    }

    public void begin() {
        txn = environment.beginTransaction();
    }

    public void rollback() {
        requireBeganTx().abort();
    }

    public void commit() {
        boolean commit = requireBeganTx().commit();
        if (!commit) {
            throw new RuntimeException("Unable to commit changes for " + txn);
        }
    }

    public Transaction currentTx() {
        return txn;
    }

    private Transaction requireBeganTx() {
        if (txn == null) {
            throw new RuntimeException("Transaction should be begun");
        }
        return txn;
    }

}
