package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionValidator implements TransactionTypeValidator {
    @Autowired
    private AccountService service;

    @Override
    public void validate(Transaction tx) {
    }

    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }
}
