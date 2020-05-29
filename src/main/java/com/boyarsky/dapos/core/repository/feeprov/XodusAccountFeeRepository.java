package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XodusAccountFeeRepository extends XodusAbstractRepository<AccountFeeAllowance> implements AccountFeeRepository {
    private static final String name = "account-fee-provider-allowance";

    @Autowired
    public XodusAccountFeeRepository(XodusRepoContext context) {
        super(name, true, context);
    }

    @Override
    @Transactional(readonly = true)
    public AccountFeeAllowance getBy(long feeProvId, AccountId accountId, boolean isSender) {
        return map(getByDbParams(List.of(
                new DbParamImpl("account", Convert.toHexString(accountId.getAddressBytes())),
                new DbParamImpl("feeProvider", feeProvId),
                new DbParamImpl("sender", isSender)
        )));
    }

    @Override
    @Transactional(readonly = true)
    public List<AccountFeeAllowance> getAll() {
        return super.getAll();
    }

    @Override
    protected AccountFeeAllowance doMap(Entity entity) {
        AccountFeeAllowance allowance = new AccountFeeAllowance();
        allowance.setAccount(AccountId.fromBytes(Convert.parseHexString((String) entity.getProperty("account"))));
        allowance.setProvId((Long) entity.getProperty("feeProvider"));
        allowance.setFeeRemaining((Long) entity.getProperty("feeRemaining"));
        allowance.setOperations((Integer) entity.getProperty("operations"));
        allowance.setSender((Boolean) entity.getProperty("sender"));
        return allowance;
    }


    @Override
    protected void storeToDbEntity(Entity e, AccountFeeAllowance accountFeeAllowance) {
        e.setProperty("account", Convert.toHexString(accountFeeAllowance.getAccount().getAddressBytes()));
        e.setProperty("feeProvider", accountFeeAllowance.getProvId());
        e.setProperty("operations", accountFeeAllowance.getOperations());
        e.setProperty("sender", accountFeeAllowance.isSender());
        e.setProperty("feeRemaining", accountFeeAllowance.getFeeRemaining());
    }

    @Override
    protected List<DbParam> idParams(AccountFeeAllowance value) {
        return List.of(new DbParamImpl("account", Convert.toHexString(value.getAccount().getAddressBytes())),
                new DbParamImpl("feeProvider", value.getProvId()),
                new DbParamImpl("sender", value.isSender()));
    }
}
