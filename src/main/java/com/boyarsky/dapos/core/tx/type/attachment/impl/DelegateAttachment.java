package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class DelegateAttachment extends AbstractAttachment {
    public DelegateAttachment(byte version) {
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
