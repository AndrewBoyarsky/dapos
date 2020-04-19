package com.boyarsky.dapos.core.tx.type.attachment;

public enum IndependentAttachmentType {
    NO_FEE(1);
    private final byte code;

    IndependentAttachmentType(int code) {
        this.code = (byte) code;
    }

    public static IndependentAttachmentType fromCode(int code) {
        for (IndependentAttachmentType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Attachment type for code " + code + " was not found");
    }
}
