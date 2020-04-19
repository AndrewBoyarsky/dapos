package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.ErrorCode;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TxException;

public class TxHandlingException extends TxException {

    public TxHandlingException(String message, Throwable cause, Transaction tx, ErrorCode code) {
        super(message, cause, tx, code);
    }
}
