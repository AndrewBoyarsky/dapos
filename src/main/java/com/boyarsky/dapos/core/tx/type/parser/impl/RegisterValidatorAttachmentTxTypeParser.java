package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class RegisterValidatorAttachmentTxTypeParser implements AttachmentTxTypeParser<RegisterValidatorAttachment> {
    @Override
    public RegisterValidatorAttachment parseAttachment(ByteBuffer buffer) {
        return new RegisterValidatorAttachment(buffer);
    }

    @Override
    public TxType type() {
        return TxType.REGISTER_VALIDATOR;
    }
}
