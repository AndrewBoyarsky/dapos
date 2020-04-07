package com.boyarsky.dapos.core;

public enum TxType {
    PAYMENT(1),PAYMENT_NO_FEE(2);
    private final byte code;
    private int gas;

    TxType(int code) {
        this.code = (byte) code;
    }

    public static TxType ofCode(int code) {
        for (TxType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Incorrect code of tx type " + code);
    }
}
