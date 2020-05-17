package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class ValidatorAttachment extends AbstractAttachment {
    private final boolean enable;
    private final short fee;
    private final byte[] publicKey;
    private final AccountId id;

    public ValidatorAttachment(byte version, boolean enable, short fee, byte[] publicKey, AccountId id) {
        super(version);
        if (publicKey != null && id != null) {
            throw new IllegalArgumentException("Public key and id should not be present at the same time");
        }
        if (publicKey == null && id == null) {
            throw new IllegalArgumentException("Public key or id should not be null");
        }
        this.enable = enable;
        this.id = id;
        this.fee = fee;
        this.publicKey = publicKey;
    }

    public ValidatorAttachment(ByteBuffer buffer) {
        super(buffer);
        enable = ByteSerializable.getBoolean(buffer);
        fee = buffer.getShort();
        byte b = buffer.get();
        if (b == 0) {
            publicKey = new byte[32];
            buffer.get(publicKey);
            id = null;
        } else {
            publicKey = null;
            id = AccountId.fromBytes(buffer);
        }
    }

    @Override
    public int mySize() {
        return 1 + 2 + 1 + (publicKey == null ? id.size() : publicKey.length);
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        ByteSerializable.putBoolean(buffer, enable);
        buffer.putShort(fee);
        buffer.put(publicKey);
    }
}
