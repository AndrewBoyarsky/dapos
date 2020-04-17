package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.ValidatorAttachment;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ValidatorAttachmentParser implements AttachmentParser<ValidatorAttachment> {
    @Override
    public ValidatorAttachment parseAttachment(ByteBuffer buffer) {
        return new ValidatorAttachment(buffer);
    }

    @Override
    public TxType type() {
        return TxType.VALIDATOR;
    }
}
