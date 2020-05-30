package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.repository.DbParam;
import com.boyarsky.dapos.core.repository.DbParamImpl;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XodusCurrencyRepository extends XodusAbstractRepository<Currency> implements CurrencyRepository {

    @Autowired
    XodusCurrencyRepository(@NonNull XodusRepoContext context) {
        super("currency", true, context);
    }

    @Override
    protected void storeToDbEntity(Entity e, Currency currency) {
        e.setProperty("id", currency.getCurrencyId());
        e.setProperty("code", currency.getCode());
        e.setProperty("name", currency.getName());
        e.setProperty("description", currency.getDescription());
        e.setProperty("issuer", Convert.toHexString(currency.getIssuer().getAddressBytes()));
        e.setProperty("decimals", currency.getDecimals());
        e.setProperty("reserve", currency.getReserve());
        e.setProperty("supply", currency.getSupply());
    }

    @Override
    protected Currency doMap(Entity e) {
        Currency currency = new Currency();
        currency.setCurrencyId((Long) e.getProperty("id"));
        currency.setCode((String) e.getProperty("code"));
        currency.setName((String) e.getProperty("name"));
        currency.setDescription((String) e.getProperty("description"));
        currency.setIssuer(AccountId.fromBytes(Convert.parseHexString((String) e.getProperty("issuer"))));
        currency.setDecimals((Byte) e.getProperty("decimals"));
        currency.setReserve((Long) e.getProperty("reserve"));
        currency.setSupply((Long) e.getProperty("supply"));
        return currency;
    }

    @Override
    @Transactional(readonly = true)
    public Currency get(long currencyId) {
        return CollectionUtils.requireAtMostOne(getAll(new DbParamImpl("id", currencyId)));
    }

    @Override
    @Transactional(readonly = true)
    public Currency getByCode(String code) {
        return CollectionUtils.requireAtMostOne(getAll(new DbParamImpl("code", code)));
    }

    @Override
    @Transactional(readonly = true)
    public List<Currency> getAll(Pagination pagination) {
        return super.getAll(pagination);
    }

    @Override
    protected List<DbParam> idParams(Currency value) {
        return List.of(
                new DbParamImpl("id", value.getCurrencyId())
        );
    }
}
