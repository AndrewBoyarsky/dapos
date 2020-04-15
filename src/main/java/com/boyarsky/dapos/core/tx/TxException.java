package com.boyarsky.dapos.core.tx;

import lombok.Getter;

@Getter
public class TxException extends RuntimeException {
    private Transaction tx;
    private ErrorCode code;

    public TxException(String message, Throwable cause, Transaction tx, ErrorCode code) {
        super(message, cause);
        this.tx = tx;
        this.code = code;
    }
}
