package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class VoteAttachmentParser implements AttachmentTxTypeParser<VoteAttachment> {
    @Override
    public TxType type() {
        return TxType.VOTE;
    }

    @Override
    public VoteAttachment parseAttachment(ByteBuffer buffer) {
        return new VoteAttachment();
    }
}
