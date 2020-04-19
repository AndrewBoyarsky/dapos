package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class MessageAttachmentTxTypeParser implements AttachmentTxTypeParser<MessageAttachment> {
    @Override
    public MessageAttachment parseAttachment(ByteBuffer buffer) {
        return new MessageAttachment(buffer);
    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
