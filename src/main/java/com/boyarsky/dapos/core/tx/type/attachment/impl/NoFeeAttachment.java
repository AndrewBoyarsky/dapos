package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class NoFeeAttachment extends AbstractAttachment {
    private final long payer;

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
