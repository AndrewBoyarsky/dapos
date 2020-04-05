package com.boyarsky.dapos.core;

public enum TxType {
    PAYMENT(1),PAYMENT_NO_FEE(2);
    private final byte code;

    TxType(int code) {
        this.code = (byte) code;
    }
}
