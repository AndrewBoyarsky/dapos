package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class CurrencyIssuanceAttachment extends AbstractAttachment {
    private String code;
    private String name;
    private String description;
    private long supply;
    private byte decimals;

    public CurrencyIssuanceAttachment(byte version, String code, String name, String description, long supply, byte decimals) {
        super(version);
        this.code = code;
        this.name = name;
        this.description = description;
        this.supply = supply;
        this.decimals = decimals;
    }

    public CurrencyIssuanceAttachment(ByteBuffer buffer) {
        super(buffer);
        byte codeSize = buffer.get();
        byte[] codeBytes = new byte[codeSize];
        buffer.get(codeBytes);
        this.code = new String(codeBytes, StandardCharsets.UTF_8);

        byte nameSize = buffer.get();
        byte[] nameBytes = new byte[nameSize];
        buffer.get(nameBytes);
        this.name = new String(nameBytes, StandardCharsets.UTF_8);

        short descSize = buffer.getShort();
        byte[] descBytes = new byte[descSize];
        buffer.get(descBytes);
        this.description = new String(descBytes, StandardCharsets.UTF_8);

        this.supply = buffer.getLong();
        this.decimals = buffer.get();
    }

    @Override
    public int mySize() {
        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);
        return 1 + codeBytes.length
                + 1 + nameBytes.length
                + 2 + descriptionBytes.length
                + 8
                + 1;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) codeBytes.length);
        buffer.put(codeBytes);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) nameBytes.length);
        buffer.put(nameBytes);
        byte[] descriptionBytes = description.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short) descriptionBytes.length);
        buffer.put(descriptionBytes);
        buffer.putLong(supply);
        buffer.put(decimals);

    }
}
