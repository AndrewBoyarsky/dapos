package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorControlAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class StartValidatorAttachemntParser implements AttachmentTxTypeParser<ValidatorControlAttachment> {
    @Override
    public TxType type() {
        return TxType.START_VALIDATOR;
    }

    @Override
    public ValidatorControlAttachment parseAttachment(ByteBuffer buffer) {
        return new ValidatorControlAttachment(buffer);
    }
}
