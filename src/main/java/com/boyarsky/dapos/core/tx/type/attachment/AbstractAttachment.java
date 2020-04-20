package com.boyarsky.dapos.core.tx.type.attachment;

import java.nio.ByteBuffer;
import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAttachment that = (AbstractAttachment) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    public abstract void putMyBytes(ByteBuffer buffer);

    public abstract int mySize();
}
