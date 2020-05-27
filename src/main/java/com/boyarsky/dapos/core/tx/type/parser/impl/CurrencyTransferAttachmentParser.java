package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyTransferAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class CurrencyTransferAttachmentParser implements AttachmentTxTypeParser<CurrencyTransferAttachment> {
    @Override
    public TxType type() {
        return TxType.CURRENCY_TRANSFER;
    }

    @Override
    public CurrencyTransferAttachment parseAttachment(ByteBuffer buffer) {
        return new CurrencyTransferAttachment(buffer);
    }
}
