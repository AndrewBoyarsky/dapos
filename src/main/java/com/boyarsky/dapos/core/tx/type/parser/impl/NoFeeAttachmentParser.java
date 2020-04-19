package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.attachment.IndependentAttachmentType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.parser.IndependentAttachmentParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class NoFeeAttachmentParser implements IndependentAttachmentParser<NoFeeAttachment> {
    @Override
    public IndependentAttachmentType type() {
        return IndependentAttachmentType.NO_FEE;
    }

    @Override
    public NoFeeAttachment parseAttachment(ByteBuffer buffer) {
        return new NoFeeAttachment(buffer);
    }
}
