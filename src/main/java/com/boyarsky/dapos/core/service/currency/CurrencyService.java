package com.boyarsky.dapos.core.service.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyTransferAttachment;

import java.util.List;

public interface CurrencyService {
    void add(Transaction tx, CurrencyIssuanceAttachment attachment);

    void transfer(Transaction tx, CurrencyTransferAttachment attachment);

    List<CurrencyHolder> holders(long currencyId);

    List<Currency> getAllCurrencies();

    Currency getById(long currencyId);

    List<CurrencyHolder> accountCurrencies(AccountId accountId);

    CurrencyHolder getCurrencyHolder(AccountId accountId, long currencyId);

    boolean reserved(String code);
}
