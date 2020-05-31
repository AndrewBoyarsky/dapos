package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyMultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class MultiCurrencyTransferAttachmentParser implements AttachmentTxTypeParser<CurrencyMultiAccountAttachment> {
    @Override
    public TxType type() {
        return TxType.MULTI_CURRENCY_TRANSFER;
    }

    @Override
    public CurrencyMultiAccountAttachment parseAttachment(ByteBuffer buffer) {
        return new CurrencyMultiAccountAttachment(buffer);
    }
}
