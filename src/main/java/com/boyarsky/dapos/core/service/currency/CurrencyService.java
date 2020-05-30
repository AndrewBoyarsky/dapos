package com.boyarsky.dapos.core.service.currency;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;

import java.util.List;

public interface CurrencyService {
    void add(Transaction tx, CurrencyIssuanceAttachment attachment);

    void transfer(Transaction tx, CurrencyIdAttachment attachment);

    List<CurrencyHolder> holders(long currencyId, Pagination pagination);

    List<Currency> getAllCurrencies(Pagination pagination);

    Currency getById(long currencyId);

    List<CurrencyHolder> accountCurrencies(AccountId accountId, Pagination pagination);

    CurrencyHolder getCurrencyHolder(AccountId accountId, long currencyId);

    Currency getByCode(String code);

    boolean reserved(String code);

    void claimReserve(Transaction tx, CurrencyIdAttachment idAttachment);
}
