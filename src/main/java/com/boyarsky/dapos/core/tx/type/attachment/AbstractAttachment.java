package com.boyarsky.dapos.core.tx.type.attachment;

import java.nio.ByteBuffer;


public abstract class AbstractAttachment implements Attachment {
    private final byte version;

    public AbstractAttachment(byte version) {
        this.version = version;
    }

    public AbstractAttachment(ByteBuffer buffer) {
        version = buffer.get();
    }

    @Override
    public int size() {
        return 1 + mySize();
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        buffer.put(version);
        putMyBytes(buffer);
    }

    public abstract void putMyBytes(ByteBuffer buffer);

    public abstract int mySize();
}
