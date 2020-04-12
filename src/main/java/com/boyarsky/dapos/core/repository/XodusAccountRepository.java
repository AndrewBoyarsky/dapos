package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XodusAccountRepository implements AccountRepository {
    private static final String entityType = "account";
    private final PersistentEntityStore store;
    private final TransactionManager manager;


    @Autowired
    public XodusAccountRepository(PersistentEntityStore store, TransactionManager manager) {
        this.store = store;
        this.manager = manager;
    }

    @Override
    public Account find(AccountId accountId) {
        StoreTransaction tx = manager.currentTx();
        boolean beginTransaction = false;

        if (tx == null) {
            tx = store.beginReadonlyTransaction();
            beginTransaction = true;
        }
        try {
            Entity entity = find(accountId, tx);
            if (entity == null) {
                return null;
            }
            return map(entity);
        } finally {
            if (beginTransaction) {
                tx.abort();
            }
        }
    }

    private Entity find(AccountId accountId, StoreTransaction txn) {
        return CollectionUtils.requireAtMostOne(txn.find(entityType, "id", Convert.toHexString(accountId.getAddressBytes())));
    }

    @Override
    public void save(Account account) {
        StoreTransaction tx = manager.currentTx(); // TODO try to inject transaction via Spring AOP or just manage transactions outside the main code
        if (tx == null) {
            throw new IllegalStateException("Not in transaction");
        }
        try {
            Entity entity;
            if (account.getId() == null) {
                entity = tx.newEntity(entityType);
                entity.setProperty("id", Convert.toHexString(account.getCryptoId().getAddressBytes()));
            } else {
                entity = tx.getEntity(account.getId());
            }
            entity.setProperty("balance", account.getBalance());
            entity.setProperty("publicKey", Convert.toHexString(account.getPublicKey()));
            entity.setProperty("type", account.getType().getCode());
            entity.setProperty("height", account.getHeight());
        } catch (Exception e) {
            tx.revert();
            throw new RuntimeException(e);
        }
    }

    private Account map(Entity entity) {
        Account acc = new Account();
        acc.setId(entity.getId());
        acc.setBalance((Long) entity.getProperty("balance"));
        acc.setCryptoId(AccountId.fromBytes(Convert.parseHexString((String) entity.getProperty("id"))));
        acc.setPublicKey(Convert.parseHexString((String) entity.getProperty("publicKey")));
        acc.setType(Account.Type.fromCode((Byte) entity.getProperty("type")));
        acc.setHeight((Long) entity.getProperty("height"));
        return acc;
    }

    @Override
    public List<Account> getAll() {
        StoreTransaction tx = manager.currentTx();
        boolean beginTransaction = false;

        if (tx == null) {
            tx = store.beginReadonlyTransaction();
            beginTransaction = true;
        }
        try {
            List<Account> accounts = new ArrayList<>();
            EntityIterable all = tx.getAll(entityType);
            for (Entity entity : all) {
                accounts.add(map(entity));
            }
            return accounts;
        } finally {
            if (beginTransaction) {
                tx.abort();
            }
        }
    }

    @Override
    public void delete(Account account) {
        throw new UnsupportedOperationException("Account deletion is not supported yet");
    }
}
