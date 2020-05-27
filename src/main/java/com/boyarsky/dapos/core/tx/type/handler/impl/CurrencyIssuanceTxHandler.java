package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyIssuanceTxHandler implements TransactionTypeHandler {

    @Autowired
    public CurrencyIssuanceTxHandler() {

    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_ISSUANCE;
    }
}
