package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiAccountAttachment extends AbstractAttachment {
    private final Map<AccountId, Long> transfers = new LinkedHashMap<>();

    public MultiAccountAttachment(byte version, Map<AccountId, Long> accountAmounts) {
        super(version);
        this.transfers.putAll(accountAmounts);
    }

    public MultiAccountAttachment(ByteBuffer buffer) {
        super(buffer);
        short accountSize = buffer.getShort();
        for (int i = 0; i < accountSize; i++) {
            transfers.put(AccountId.fromBytes(buffer), buffer.getLong());
        }
    }

    public Map<AccountId, Long> getTransfers() {
        return transfers;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        buffer.putShort((short) transfers.size());
        for (Map.Entry<AccountId, Long> entry : transfers.entrySet()) {
            entry.getKey().putBytes(buffer);
            buffer.putLong(entry.getValue());
        }
    }

    @Override
    public int mySize() {
        return 2 + 8 * transfers.size() + transfers.keySet().stream().mapToInt(AccountId::size).sum();
    }
}
