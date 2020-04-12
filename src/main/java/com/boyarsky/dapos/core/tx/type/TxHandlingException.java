package com.boyarsky.dapos.core.tx.type;

import com.boyarsky.dapos.core.tx.Transaction;

public class TxHandlingException extends RuntimeException {
    private Transaction tx;

    public TxHandlingException(String message, Transaction tx) {
        super(message);
        this.tx = tx;
    }
}
