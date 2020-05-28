package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyReserveClaimTxHandler implements TransactionTypeHandler {
    private CurrencyService currencyService;

    @Autowired
    public CurrencyReserveClaimTxHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        currencyService.claimReserve(tx, tx.getAttachment(CurrencyIdAttachment.class));
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_CLAIM_RESERVE;
    }
}
