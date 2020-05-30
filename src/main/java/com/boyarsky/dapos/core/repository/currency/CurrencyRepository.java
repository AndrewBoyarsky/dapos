package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface CurrencyRepository {
    void save(Currency newCurrency);

    void remove(Currency newCurrency);

    Currency get(long currencyId);

    Currency getByCode(String code);

    List<Currency> getAll(Pagination pagination);
}
