package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;

import java.util.List;

public interface CurrencyHolderRepository {
    void save(CurrencyHolder account);

    CurrencyHolder get(AccountId account, long currencyId);

    void remove(CurrencyHolder holder);

    List<CurrencyHolder> getAllForCurrency(long currencyId);

    List<CurrencyHolder> getAllByAccount(AccountId accountId);
}
