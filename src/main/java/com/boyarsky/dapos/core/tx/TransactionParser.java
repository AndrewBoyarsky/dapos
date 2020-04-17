package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionParser {
    private Map<TxType, AttachmentParser<? extends AbstractAttachment>> parsers = new HashMap<>();

    @Autowired
    public TransactionParser(Map<TxType, AttachmentParser<? extends AbstractAttachment>> parsers) {
        this.parsers.putAll(parsers);
    }

    public Transaction parseTx(byte[] txBytes) {
        Transaction transaction = new Transaction(txBytes);
        AttachmentParser<? extends AbstractAttachment> attachmentParser = parsers.get(transaction.getType());
        ByteBuffer attachmentBuffer = ByteBuffer.wrap(transaction.getData());
        AbstractAttachment attachment = attachmentParser.parseAttachment(attachmentBuffer);
        transaction.putAttachment(attachment);
        if (attachmentBuffer.position() != attachmentBuffer.capacity()) {
            byte notTypedAttachmentPresentByte = attachmentBuffer.get();
            if (notTypedAttachmentPresentByte != 0) {
                if ((notTypedAttachmentPresentByte & 1) == 1) {
                    // parse message
                }
                if ((notTypedAttachmentPresentByte & 2) == 2) {
                    // parse no fee attachment
                }
            }
        }

        return transaction;
    }
}
