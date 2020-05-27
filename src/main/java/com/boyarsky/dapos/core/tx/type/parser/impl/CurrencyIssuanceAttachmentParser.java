package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class CurrencyIssuanceAttachmentParser implements AttachmentTxTypeParser<CurrencyIssuanceAttachment> {
    @Override
    public TxType type() {
        return TxType.CURRENCY_ISSUANCE;
    }

    @Override
    public CurrencyIssuanceAttachment parseAttachment(ByteBuffer buffer) {
        return new CurrencyIssuanceAttachment(buffer);
    }
}
