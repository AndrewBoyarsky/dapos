package com.boyarsky.dapos.web.exception;

public class RestValidationException extends RuntimeException {
    public RestValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestValidationException(String message) {
        super(message);
    }
}
