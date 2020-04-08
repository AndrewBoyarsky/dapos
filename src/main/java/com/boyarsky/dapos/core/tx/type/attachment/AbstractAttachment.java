package com.boyarsky.dapos.core.tx.type.attachment;

import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public abstract class AbstractAttachment implements ByteSerializable {
    private byte version;

    public AbstractAttachment(byte version) {
        this.version = version;
    }

    public AbstractAttachment(ByteBuffer buffer) {
        version = buffer.get();
    }

    public long fullSize() {
        return 1 + size();
    }

    public void putAllBytes(ByteBuffer buffer) {
        buffer.put(version);
        putBytes(buffer);
    }
}
