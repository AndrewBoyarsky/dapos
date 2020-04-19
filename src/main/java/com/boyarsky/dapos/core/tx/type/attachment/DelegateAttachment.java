package com.boyarsky.dapos.core.tx.type.attachment;

import java.nio.ByteBuffer;

public class DelegateAttachment extends AbstractAttachment {
    public DelegateAttachment(byte version) {
        super(version);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void putBytes(ByteBuffer buffer) {

    }
}
