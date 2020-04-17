package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.MessageAttachment;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class MessageAttachmentParser implements AttachmentParser<MessageAttachment> {
    @Override
    public MessageAttachment parseAttachment(ByteBuffer buffer) {
        return new MessageAttachment(buffer);
    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
