package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class ClaimAttachment extends AbstractAttachment {
    public ClaimAttachment(byte version) {
        super(version);
    }

    @Override
    public int mySize() {
        return 0;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {

    }
}
