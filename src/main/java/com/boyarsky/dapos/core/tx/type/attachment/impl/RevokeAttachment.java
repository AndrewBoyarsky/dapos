package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class RevokeAttachment extends AbstractAttachment {

    public RevokeAttachment(byte version) {
        super(version);
    }

    public RevokeAttachment(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public int mySize() {
        return 0;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
    }
}
