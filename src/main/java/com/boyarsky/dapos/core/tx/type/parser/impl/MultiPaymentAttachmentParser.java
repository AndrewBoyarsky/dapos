package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class MultiPaymentAttachmentParser implements AttachmentTxTypeParser<MultiAccountAttachment> {
    @Override
    public TxType type() {
        return TxType.MULTI_PAYMENT;
    }

    @Override
    public MultiAccountAttachment parseAttachment(ByteBuffer buffer) {
        return new MultiAccountAttachment(buffer);
    }
}
