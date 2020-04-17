package com.boyarsky.dapos.core.crypto;

import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedData implements ByteSerializable {
    private byte[] encrypted;
    private byte[] nonce;

    public EncryptedData(ByteBuffer buffer) {
        this.encrypted = new byte[buffer.getShort()];
        buffer.get(encrypted);
        this.nonce = new byte[buffer.get()];
        buffer.get(nonce);
    }

    @Override
    public int size() {
        return encrypted.length + 2 + nonce.length + 1;
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        buffer.putShort((short) encrypted.length);
        buffer.put(encrypted);
        buffer.put((byte) nonce.length);
        buffer.put(nonce);
    }
}
