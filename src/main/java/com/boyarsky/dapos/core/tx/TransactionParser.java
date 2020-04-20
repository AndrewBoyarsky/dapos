package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;
import com.boyarsky.dapos.core.tx.type.attachment.IndependentAttachmentType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import com.boyarsky.dapos.core.tx.type.parser.IndependentAttachmentParser;
import com.boyarsky.dapos.core.tx.type.parser.TxParsingException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionParser {
    private final Map<TxType, AttachmentTxTypeParser<? extends Attachment>> txTypeParsers = new HashMap<>();
    private final Map<IndependentAttachmentType, IndependentAttachmentParser<? extends AbstractAttachment>> indAttachmentParsers = new HashMap<>();

    public TransactionParser(Map<TxType, AttachmentTxTypeParser<? extends Attachment>> txTypeParsers, Map<IndependentAttachmentType, IndependentAttachmentParser<? extends AbstractAttachment>> indAttachmentParsers) {
        this.txTypeParsers.putAll(txTypeParsers);
        this.indAttachmentParsers.putAll(indAttachmentParsers);
    }

    public Transaction parseTx(byte[] txBytes) {
        Transaction transaction = new Transaction(txBytes);
        AttachmentTxTypeParser<? extends Attachment> attachmentTxTypeParser = txTypeParsers.get(transaction.getType());
        ByteBuffer attachmentBuffer = ByteBuffer.wrap(transaction.getData());
        Attachment attachment = attachmentTxTypeParser.parseAttachment(attachmentBuffer);
        transaction.putAttachment(attachment);
        if (attachmentBuffer.position() != attachmentBuffer.capacity()) {
            byte notTypedAttachmentPresentByte = attachmentBuffer.get();
            if (notTypedAttachmentPresentByte != 0) {
                if ((notTypedAttachmentPresentByte & 1) == 1) {
                    AttachmentTxTypeParser<? extends Attachment> messageParser = txTypeParsers.get(TxType.MESSAGE);
                    if (transaction.getType() == TxType.MESSAGE) {
                        throw new TxParsingException("Duplicate message for MESSAGE tx", null, transaction, ErrorCodes.DUPLICATE_MESSAGE_PARSING_ERROR);
                    }
                    MessageAttachment messageAttachment = (MessageAttachment) messageParser.parseAttachment(attachmentBuffer);
                    transaction.putAttachment(messageAttachment);
                }
                if ((notTypedAttachmentPresentByte & 2) == 2) {
                    IndependentAttachmentParser<? extends AbstractAttachment> parser = indAttachmentParsers.get(IndependentAttachmentType.NO_FEE);
                    NoFeeAttachment noFeeAttachment = (NoFeeAttachment) parser.parseAttachment(attachmentBuffer);
                    transaction.putAttachment(noFeeAttachment);
                }
            }
        }

        return transaction;
    }
}
