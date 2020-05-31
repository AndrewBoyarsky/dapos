package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ValidatorControlAttachment extends AbstractAttachment {

    public ValidatorControlAttachment(byte version) {
        super(version);
    }

    public ValidatorControlAttachment(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {

    }

    @Override
    public int mySize() {
        return 0;
    }
}
