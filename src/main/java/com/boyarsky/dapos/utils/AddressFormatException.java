package com.boyarsky.dapos.utils;

public class AddressFormatException extends RuntimeException {
    public AddressFormatException(String message) {
        super(message);
    }

    public AddressFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
