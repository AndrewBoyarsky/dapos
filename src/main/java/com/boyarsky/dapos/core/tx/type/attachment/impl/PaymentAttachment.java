package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.nio.ByteBuffer;

public class PaymentAttachment implements Attachment {
    public PaymentAttachment() {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
    }
}
