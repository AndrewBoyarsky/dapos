package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.model.currency.Currency;

import java.util.List;

public interface CurrencyRepository {
    void save(Currency newCurrency);

    Currency get(long currencyId);

    Currency getByCode(String code);

    List<Currency> getAll();
}
