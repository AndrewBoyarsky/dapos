package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public class VoteAttachment extends AbstractAttachment {
    private final long voteStake;

    public VoteAttachment(byte version, long voteStake) {
        super(version);
        this.voteStake = voteStake;
    }

    public VoteAttachment(ByteBuffer buffer) {
        super(buffer);
        this.voteStake = buffer.getLong();
    }

    @Override
    public int mySize() {
        return 8;
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(voteStake);
    }
}
