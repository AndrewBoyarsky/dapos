package com.boyarsky.dapos.core.tx.type.parser.impl;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.FeeProviderAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class FeeProviderAttachmentParser implements AttachmentTxTypeParser<FeeProviderAttachment> {
    @Override
    public TxType type() {
        return TxType.SET_FEE_PROVIDER;
    }

    @Override
    public FeeProviderAttachment parseAttachment(ByteBuffer buffer) {
        return new FeeProviderAttachment((byte) 0);
    }
}
