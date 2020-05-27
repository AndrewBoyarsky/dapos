package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyTransferTransactionValidator implements TransactionTypeValidator {
    @Autowired
    private AccountService service;

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_TRANSFER;
    }
}
