package com.boyarsky.dapos.core.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {
    private TransactionParser parser;
    private TransactionValidator validator;
    private TransactionHandler handler;

    @Autowired
    public TransactionProcessor(TransactionParser parser, TransactionValidator validator, TransactionHandler handler) {
        this.parser = parser;
        this.validator = validator;
        this.handler = handler;
    }

    public ValidationResult checkTx(byte[] tx) {
        Transaction transaction;
        try {
             transaction = parser.parseTx(tx);
        } catch (Exception e) {
            return new ValidationResult("Unable to parse transaction", 255, null, e);
        }
        return validator.validate(transaction);
    }

    public void deliverTx(byte[] tx) {
        Transaction transaction = parser.parseTx(tx);
        handler.handleTx(transaction);
    }
}
