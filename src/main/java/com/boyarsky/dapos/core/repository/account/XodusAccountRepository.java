package com.boyarsky.dapos.core.repository.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class XodusAccountRepository implements AccountRepository {
    private static final String entityType = "account";
    private final XodusRepoContext context;

    @Autowired
    public XodusAccountRepository(XodusRepoContext context) {
        this.context = context;
    }

    @Override
    @Transactional(readonly = true)
    public Account find(AccountId accountId) {
        StoreTransaction tx = context.getTx();
        Entity entity = find(accountId, tx);
        if (entity == null) {
            return null;
        }
        return map(entity);
    }

    private Entity find(AccountId accountId, StoreTransaction txn) {
        EntityIterable id = txn.find(entityType, "id", Convert.toHexString(accountId.getAddressBytes()));
        return CollectionUtils.requireAtMostOne(id);
    }

    @Override
    @Transactional(requiredExisting = true)
    public void save(Account account) {
        StoreTransaction tx = context.getTx();
        Entity entity;
        if (account.getDbId() == null) {
            entity = tx.newEntity(entityType);
            entity.setProperty("id", Convert.toHexString(account.getCryptoId().getAddressBytes()));
        } else {
            entity = tx.getEntity(account.getDbId());
        }
        entity.setProperty("balance", account.getBalance());
        if (account.getPublicKey() != null) {
            entity.setProperty("publicKey", Convert.toHexString(account.getPublicKey()));
        }

        entity.setProperty("type", account.getType().getCode());
        entity.setProperty("height", account.getHeight());
    }

    private Account map(Entity entity) {
        Account acc = new Account();
        acc.setDbId(entity.getId());
        acc.setBalance((Long) entity.getProperty("balance"));
        acc.setCryptoId(AccountId.fromBytes(Convert.parseHexString((String) entity.getProperty("id"))));
        acc.setPublicKey(Convert.parseHexString((String) entity.getProperty("publicKey")));
        acc.setType(Account.Type.fromCode((Byte) entity.getProperty("type")));
        acc.setHeight((Long) entity.getProperty("height"));
        return acc;
    }

    @Override
    @Transactional(readonly = true)
    public List<Account> getAll() {
        StoreTransaction tx = context.getTx();
        List<Account> accounts = new ArrayList<>();
        EntityIterable all = tx.getAll(entityType);
        for (Entity entity : all) {
            accounts.add(map(entity));
        }
        return accounts;
    }

    @Override
    @Transactional(requiredExisting = true)
    public void delete(Account account) {
        throw new UnsupportedOperationException("Account deletion is not supported yet");
    }
}
