package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.account.AccountId;

import java.nio.ByteBuffer;
import java.util.Map;

public class CurrencyMultiAccountAttachment extends MultiAccountAttachment {
    private final long currencyId;

    public CurrencyMultiAccountAttachment(byte version, Map<AccountId, Long> accountAmounts, long currencyId) {
        super(version, accountAmounts);
        this.currencyId = currencyId;
    }

    public CurrencyMultiAccountAttachment(ByteBuffer buffer) {
        super(buffer);
        this.currencyId = buffer.getLong();
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        super.putMyBytes(buffer);
        buffer.putLong(currencyId);
    }

    public long getCurrencyId() {
        return currencyId;
    }

    @Override
    public int mySize() {
        return super.mySize() + 8;
    }
}
