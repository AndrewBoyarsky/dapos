package com.boyarsky.dapos.core.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastSuccessBlockData {
    private byte[] appHash;
    private long height;

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(appHash.length + 8);
        buffer.putLong(height);
        buffer.put(appHash);
        return buffer.array();
    }


    public static LastSuccessBlockData fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        LastSuccessBlockData lastSuccessBlockData = new LastSuccessBlockData();
        lastSuccessBlockData.height = buffer.getLong();
        lastSuccessBlockData.appHash = new byte[bytes.length - 8];
        buffer.get(lastSuccessBlockData.appHash);
        return lastSuccessBlockData;
    }

}
