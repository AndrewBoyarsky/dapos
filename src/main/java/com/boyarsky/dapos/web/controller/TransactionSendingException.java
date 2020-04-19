package com.boyarsky.dapos.web.controller;

public class TransactionSendingException extends RuntimeException {
    public TransactionSendingException(String message) {
        super(message);
    }
}
