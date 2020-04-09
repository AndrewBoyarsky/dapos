package com.boyarsky.dapos.core.model;

public enum State {
    SUSPENDED(1), ACTIVE(2), STOPPED(3);
    private final byte code;

    public byte getCode() {
        return code;
    }

    State(int code) {
        this.code = (byte) code;
    }

    public static State ofCode(int code) {
        for (State value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unable to find State instance for code " + code);
    }
}
