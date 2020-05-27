package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class CurrencyTransferAttachment extends AbstractAttachment {
    private long currencyId;

    public CurrencyTransferAttachment(byte version, long currencyId) {
        super(version);
        this.currencyId = currencyId;
    }

    public CurrencyTransferAttachment(ByteBuffer buffer) {
        super(buffer);
        this.currencyId = buffer.getLong();
    }

    @Override
    public int mySize() {
        return 8;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(currencyId);
    }
}
