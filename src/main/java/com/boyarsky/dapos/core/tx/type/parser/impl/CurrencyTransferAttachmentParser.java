package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class CurrencyTransferAttachmentParser implements AttachmentTxTypeParser<CurrencyIdAttachment> {
    @Override
    public TxType type() {
        return TxType.CURRENCY_TRANSFER;
    }

    @Override
    public CurrencyIdAttachment parseAttachment(ByteBuffer buffer) {
        return new CurrencyIdAttachment(buffer);
    }
}
