package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.Getter;
import lombok.NonNull;

import java.nio.ByteBuffer;

@Getter
public class RegisterValidatorAttachment extends AbstractAttachment {
    private final boolean enable;
    private final short fee;
    private final byte[] publicKey;
    private final AccountId rewardId;

    public RegisterValidatorAttachment(byte version, boolean enable, short fee, @NonNull byte[] publicKey, @NonNull AccountId rewardId) {
        super(version);
        this.rewardId = rewardId;
        this.enable = enable;
        this.fee = fee;
        this.publicKey = publicKey;
    }

    public RegisterValidatorAttachment(ByteBuffer buffer) {
        super(buffer);
        enable = ByteSerializable.getBoolean(buffer);
        fee = buffer.getShort();
        publicKey = new byte[32];
        buffer.get(publicKey);
        rewardId = AccountId.fromBytes(buffer);
    }

    @Override
    public int mySize() {
        return 1 + 2 + publicKey.length + rewardId.size();
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        ByteSerializable.putBoolean(buffer, enable);
        buffer.putShort(fee);
        buffer.put(publicKey);
        rewardId.putBytes(buffer);
    }
}
