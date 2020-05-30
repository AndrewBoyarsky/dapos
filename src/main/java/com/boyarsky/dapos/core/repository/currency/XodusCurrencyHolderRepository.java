package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XodusCurrencyHolderRepository extends XodusAbstractRepository<CurrencyHolder> implements CurrencyHolderRepository {

    @Autowired
    XodusCurrencyHolderRepository(XodusRepoContext context) {
        super("currency_holder", true, context);
    }

    @Override
    protected void storeToDbEntity(Entity e, CurrencyHolder currencyHolder) {
        e.setProperty("account", Convert.toHexString(currencyHolder.getHolder().getAddressBytes()));
        e.setProperty("currency", currencyHolder.getCurrencyId());
        e.setProperty("amount", currencyHolder.getAmount());
    }

    @Override
    protected CurrencyHolder doMap(Entity e) {
        AccountId account = AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("account")));
        Long currency = (Long) e.getProperty("currency");
        Long amount = (Long) e.getProperty("amount");
        return new CurrencyHolder(0, account, currency, amount);
    }

    @Override
    @Transactional(readonly = true)
    public CurrencyHolder get(AccountId account, long currencyId) {
        return CollectionUtils.requireAtMostOne(getAll(new DbParamImpl("account", Convert.toHexString(account.getAddressBytes())), new DbParamImpl("currency", currencyId)));
    }

    @Override
    @Transactional(readonly = true)
    public List<CurrencyHolder> getAllForCurrency(long currencyId, Pagination pagination) {
        return getAll(pagination, new DbParamImpl("currency", currencyId));
    }

    @Override
    protected List<DbParam> idParams(CurrencyHolder value) {
        return List.of(
                new DbParamImpl("account", Convert.toHexString(value.getHolder().getAddressBytes())),
                new DbParamImpl("currency", value.getCurrencyId())
        );
    }

    @Override
    @Transactional(readonly = true)
    public List<CurrencyHolder> getAllByAccount(AccountId accountId, Pagination pagination) {
        return getAll(pagination, new DbParamImpl("account", Convert.toHexString(accountId.getAddressBytes())));
    }
}
