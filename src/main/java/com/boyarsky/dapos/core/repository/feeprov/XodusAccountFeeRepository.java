package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.repository.ComparableByteArray;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import jetbrains.exodus.entitystore.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        Entity entity = find(feeProvId, accountId);
        if (entity == null) {
            return null;
        }
        return map(entity);
    }

    private Entity find(long feeProvId, AccountId accountId) {
        return CollectionUtils.requireAtMostOne(context.getTx().find(name, "account", new ComparableByteArray(accountId.getAddressBytes()))
                .intersect(context.getTx().find(name, "feeProvider", feeProvId)));
    }

    private AccountFeeAllowance map(Entity entity) {
        AccountFeeAllowance allowance = new AccountFeeAllowance();
        allowance.setAccount(AccountId.fromBytes(((ComparableByteArray) entity.getProperty("account")).getData()));
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
        if (fee.getDbId() != null) {
            toSave = context.getTx().getEntity(fee.getDbId());
        }
        if (toSave == null) {
            toSave = find(fee.getProvId(), fee.getAccount());
        }
        if (toSave == null) {
            toSave = context.getTx().newEntity(name);
            toSave.setProperty("account", new ComparableByteArray(fee.getAccount().getAddressBytes()));
            toSave.setProperty("feeProvider", fee.getProvId());
        }
        toSave.setProperty("operations", fee.getOperations());
        toSave.setProperty("feeRemaining", fee.getFeeRemaining());
        toSave.setProperty("height", fee.getHeight());
    }
}
