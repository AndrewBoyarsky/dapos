package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.ErrorCode;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TxException;

public class TxParsingException extends TxException {
    public TxParsingException(String message, Throwable cause, Transaction tx, ErrorCode code) {
        super(message, cause, tx, code);
    }
}
