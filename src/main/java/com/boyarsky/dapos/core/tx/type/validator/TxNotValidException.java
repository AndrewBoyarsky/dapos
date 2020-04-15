package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.tx.ErrorCode;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TxException;

public class TxNotValidException extends TxException {

    public TxNotValidException(String message, Throwable cause, Transaction tx, ErrorCode code) {
        super(message, cause, tx, code);
    }
}
