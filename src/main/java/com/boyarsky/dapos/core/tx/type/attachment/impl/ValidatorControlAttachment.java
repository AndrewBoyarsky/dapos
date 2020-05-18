package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ValidatorControlAttachment extends AbstractAttachment {
    private final AccountId validatorId;


    public ValidatorControlAttachment(byte version, AccountId validatorId) {
        super(version);
        this.validatorId = validatorId;
    }

    public ValidatorControlAttachment(ByteBuffer buffer) {
        super(buffer);
        this.validatorId = AccountId.fromBytes(buffer);
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        this.validatorId.putBytes(buffer);
    }

    @Override
    public int mySize() {
        return this.validatorId.size();
    }
}
