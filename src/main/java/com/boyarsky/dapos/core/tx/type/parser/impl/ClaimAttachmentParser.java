package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ClaimAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ClaimAttachmentParser implements AttachmentTxTypeParser<ClaimAttachment> {
    @Override
    public TxType type() {
        return TxType.REVOKE;
    }

    @Override
    public ClaimAttachment parseAttachment(ByteBuffer buffer) {
        return new ClaimAttachment((byte) 0);
    }
}
