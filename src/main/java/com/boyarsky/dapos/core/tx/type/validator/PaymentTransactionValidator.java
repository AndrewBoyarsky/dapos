package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.beans.factory.annotation.Autowired;

public class PaymentTransactionValidator implements TransactionTypeValidator {
    @Autowired
    private AccountService service;

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getRecipient() == null) {
            throw new TxNotValidException("Recipient required for PAYMENT tx", tx, 11);
        }
    }

    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }
}
