package com.boyarsky.dapos.core.tx.type;

public enum TxType {
    PAYMENT(1),SET_FEE_PROVIDER(2), CHANGE_FEE_PROVIDER(3), CONTRACT(4), MESSAGE(5), CURRENCY(6);
    private final byte code;

    TxType(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
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
