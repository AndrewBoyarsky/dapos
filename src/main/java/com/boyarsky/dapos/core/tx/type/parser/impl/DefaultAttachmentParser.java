package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class DefaultAttachmentParser implements AttachmentTxTypeParser<AbstractAttachment> {

    @Override
    public TxType type() {
        return TxType.ALL;
    }

    @Override
    public AbstractAttachment parseAttachment(ByteBuffer buffer) {
        throw new UnsupportedOperationException("Default attachment is not currently supported");
    }
}
