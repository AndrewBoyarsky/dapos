package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionHandler implements TransactionTypeHandler {
    @Autowired
    private AccountService accountService;
    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }

    @Override
    public void handle(Transaction tx) {

    }
}
