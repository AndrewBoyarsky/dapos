package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class NoFeeAttachment extends AbstractAttachment {
    private long payer;

    public NoFeeAttachment(ByteBuffer buffer) {
        super(buffer);
        this.payer = buffer.getLong();
    }

    public NoFeeAttachment(byte version, long payer) {
        super(version);
        this.payer = payer;
    }

    @Override
    public int mySize() {
        return 8;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(payer);
    }
}
