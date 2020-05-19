package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RevokeAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class RevokeAttachmentParser implements AttachmentTxTypeParser<RevokeAttachment> {
    @Override
    public TxType type() {
        return TxType.REVOKE;
    }

    @Override
    public RevokeAttachment parseAttachment(ByteBuffer buffer) {
        return new RevokeAttachment(buffer);
    }
}
