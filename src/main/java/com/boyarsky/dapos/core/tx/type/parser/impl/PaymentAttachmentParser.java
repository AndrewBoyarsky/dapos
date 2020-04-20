package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class PaymentAttachmentParser implements AttachmentTxTypeParser<PaymentAttachment> {

    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }

    @Override
    public PaymentAttachment parseAttachment(ByteBuffer buffer) {
        return new PaymentAttachment();
    }
}
