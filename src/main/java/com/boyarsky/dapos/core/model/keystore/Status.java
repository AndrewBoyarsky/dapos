package com.boyarsky.dapos.core.model.keystore;

public enum Status {
    NOT_FOUND("Bad credentials"),
    DELETE_ERROR("Internal delete error"),
    DUPLICATE_FOUND("Already exist"),
    BAD_CREDENTIALS("Bad credentials"),
    READ_ERROR("Internal read error"),
    WRITE_ERROR("Internal write error"),
    DECRYPTION_ERROR("Bad credentials"),
    NOT_AVAILABLE("Something went wrong"),
    OK("OK");

    Status(String message) {
            this.message = message;
        }
        private final String message;

        public boolean isOK(){
            return this.message.equals(Status.OK.message);
        }

        public boolean isDuplicate(){
            return this.message.equals(Status.DUPLICATE_FOUND.message);
        }

    }