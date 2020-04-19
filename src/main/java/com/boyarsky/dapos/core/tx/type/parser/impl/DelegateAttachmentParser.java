package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.DelegateAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class DelegateAttachmentParser implements AttachmentTxTypeParser<DelegateAttachment> {
    @Override
    public TxType type() {
        return TxType.DELEGATE;
    }

    @Override
    public DelegateAttachment parseAttachment(ByteBuffer buffer) {
        return new DelegateAttachment((byte) 0);
    }
}
