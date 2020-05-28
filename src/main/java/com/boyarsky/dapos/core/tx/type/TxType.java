package com.boyarsky.dapos.core.tx.type;

public enum TxType {
    PAYMENT(1), SET_FEE_PROVIDER(2), REGISTER_VALIDATOR(3), MESSAGE(4), VOTE(7), REVOKE(8), STOP_VALIDATOR(9), START_VALIDATOR(10), UPDATE_VALIDATOR(11), CURRENCY_ISSUANCE(12), CURRENCY_TRANSFER(13), CURRENCY_CLAIM_RESERVE(14), CURRENCY_LIQUIDATE(15), ALL(-1);
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
