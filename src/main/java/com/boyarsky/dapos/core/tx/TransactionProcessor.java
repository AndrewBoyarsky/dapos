package com.boyarsky.dapos.core.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {
    private TransactionParser parser;
    private TransactionValidator validator;

    @Autowired
    public TransactionProcessor(TransactionParser parser, TransactionValidator validator) {
        this.parser = parser;
        this.validator = validator;
    }

    public int checkTx(byte[] tx) {
        Transaction transaction = parser.parseTx(tx);
        return validator.validate(transaction);
    }

    public void deliverTx(byte[] tx) {

    }
}
