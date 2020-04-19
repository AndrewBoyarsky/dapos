package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.ByteSerializable;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class ValidatorAttachment extends AbstractAttachment {
    private boolean enable;
    private short fee;

    public ValidatorAttachment(byte version, boolean enable, short fee) {
        super(version);
        this.enable = enable;
        this.fee = fee;
    }

    public ValidatorAttachment(ByteBuffer buffer) {
        super(buffer);
        enable = ByteSerializable.getBoolean(buffer);
        fee = buffer.getShort();
    }

    @Override
    public int mySize() {
        return 1 + 2;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        ByteSerializable.putBoolean(buffer, enable);
        buffer.putShort(fee);
    }
}
