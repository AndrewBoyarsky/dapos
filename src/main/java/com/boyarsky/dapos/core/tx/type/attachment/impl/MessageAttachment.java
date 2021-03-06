package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class MessageAttachment extends AbstractAttachment {
    private final EncryptedData encryptedData;
    private final boolean compressed;
    private final boolean toSelf;

    public MessageAttachment(ByteBuffer buffer) {
        super(buffer);
        encryptedData = new EncryptedData(buffer);
        compressed = ByteSerializable.getBoolean(buffer);
        toSelf = ByteSerializable.getBoolean(buffer);
    }

    public MessageAttachment(byte version, EncryptedData encryptedData, boolean compressed, boolean toSelf) {
        super(version);
        this.encryptedData = encryptedData;
        this.compressed = compressed;
        this.toSelf = toSelf;
    }

    @Override
    public int mySize() {
        return encryptedData.size() + 1 + 1;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        encryptedData.putBytes(buffer);
        ByteSerializable.putBoolean(buffer, compressed);
        ByteSerializable.putBoolean(buffer, toSelf);
    }
}
