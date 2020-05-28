package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyIssuanceTxHandler implements TransactionTypeHandler {
    private CurrencyService currencyService;

    @Autowired
    public CurrencyIssuanceTxHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        currencyService.add(tx, tx.getAttachment(CurrencyIssuanceAttachment.class));
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_ISSUANCE;
    }
}
