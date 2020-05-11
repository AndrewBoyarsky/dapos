package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XodusAccountFeeRepository implements AccountFeeRepository {
    private String name = "accountFee";
    private XodusRepoContext context;

    @Autowired
    public XodusAccountFeeRepository(XodusRepoContext context) {
        this.context = context;
    }

    @Override
    @Transactional(readonly = true)
    public AccountFeeAllowance getBy(long feeProvId, AccountId accountId) {
        Entity entity = find(feeProvId, accountId, context.getTx());
        if (entity == null) {
            return null;
        }
        return map(entity);
    }

    @Override
    @Transactional(readonly = true)
    public List<AccountFeeAllowance> getAll() {
        EntityIterable all = context.getTx().getAll(name);
        List<AccountFeeAllowance> list = new ArrayList<>();
        for (Entity entity : all) {
            list.add(map(entity));
        }
        return list;
    }

    private Entity find(long feeProvId, AccountId accountId, StoreTransaction tx) {
        EntityIterable found = tx.find(name, "account", Convert.toHexString(accountId.getAddressBytes()));
        EntityIterable feeProvider = tx.find(name, "feeProvider", feeProvId);
        return CollectionUtils.requireAtMostOne(found
                .intersect(feeProvider));
    }

    private AccountFeeAllowance map(Entity entity) {
        AccountFeeAllowance allowance = new AccountFeeAllowance();
        allowance.setAccount(AccountId.fromBytes(Convert.parseHexString((String) entity.getProperty("account"))));
        allowance.setProvId((Long) entity.getProperty("feeProvider"));
        allowance.setFeeRemaining((Long) entity.getProperty("feeRemaining"));
        allowance.setOperations((Integer) entity.getProperty("operations"));
        allowance.setHeight((Long) entity.getProperty("height"));
        return allowance;
    }

    @Override
    @Transactional(requiredExisting = true)
    public void save(AccountFeeAllowance fee) {
        Entity toSave = null;
        StoreTransaction tx = context.getTx();
        if (fee.getDbId() != null) {
            toSave = tx.getEntity(fee.getDbId());
        }
        if (toSave == null) {
            toSave = find(fee.getProvId(), fee.getAccount(), tx);
        }
        if (toSave == null) {
            toSave = tx.newEntity(name);
            toSave.setProperty("account", Convert.toHexString(fee.getAccount().getAddressBytes()));
            toSave.setProperty("feeProvider", fee.getProvId());
        }
        toSave.setProperty("operations", fee.getOperations());
        toSave.setProperty("feeRemaining", fee.getFeeRemaining());
        toSave.setProperty("height", fee.getHeight());
    }
}
