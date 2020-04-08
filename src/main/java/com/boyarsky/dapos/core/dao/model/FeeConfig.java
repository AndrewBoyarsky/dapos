package com.boyarsky.dapos.core.dao.model;

import com.boyarsky.dapos.core.tx.ByteSerializable;
import com.boyarsky.dapos.core.tx.type.TxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeConfig implements ByteSerializable {
    private static final byte defaultValue = -1;
    private long maxAllowedFee;
    private int maxAllowedOperations;
    private long maxAllowedTotalFee;
    private Set<TxType> allowedTxs = new HashSet<>();
    private boolean allowedAllTxs;

    @Override
    public int size() {
        return ByteSerializable.longSize(maxAllowedFee, defaultValue) + ByteSerializable.intSize(maxAllowedOperations, defaultValue)
                + ByteSerializable.longSize(maxAllowedTotalFee, defaultValue) + (allowedAllTxs ? 1 : 2 + allowedTxs.size());
    }

    public FeeConfig(ByteBuffer buffer) {
        maxAllowedFee = ByteSerializable.getDefaultLong(buffer, defaultValue);
        maxAllowedOperations = ByteSerializable.getDefaultInt(buffer, defaultValue);
        maxAllowedTotalFee = ByteSerializable.getDefaultLong(buffer, defaultValue);
        byte b = buffer.get();
        allowedAllTxs = b == 1;
        byte allowedSize = buffer.get();
        for (int i = 0; i < allowedSize; i++) {
            allowedTxs.add(TxType.ofCode(buffer.get()));
        }
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        ByteSerializable.putDefaultLong(buffer, maxAllowedFee, defaultValue);
        ByteSerializable.putDefaultInt(buffer, maxAllowedOperations, defaultValue);
        ByteSerializable.putDefaultLong(buffer, maxAllowedTotalFee, defaultValue);
        if (allowedAllTxs) {
            buffer.put((byte) 1);
        } else {
            buffer.put((byte) 0);
            buffer.put((byte) allowedTxs.size());
            for (TxType allowedTx : allowedTxs) {
                buffer.put(allowedTx.getCode());
            }
        }
    }
}
