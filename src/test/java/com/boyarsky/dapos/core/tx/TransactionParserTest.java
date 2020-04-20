package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.IndependentAttachmentType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.parser.impl.DefaultAttachmentParser;
import com.boyarsky.dapos.core.tx.type.parser.impl.MessageAttachmentTxTypeParser;
import com.boyarsky.dapos.core.tx.type.parser.impl.NoFeeAttachmentParser;
import com.boyarsky.dapos.core.tx.type.parser.impl.PaymentAttachmentParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionParserTest {
    TransactionParser transactionParser = new TransactionParser(
            Map.of(TxType.PAYMENT, new PaymentAttachmentParser(), TxType.ALL, new DefaultAttachmentParser(), TxType.MESSAGE, new MessageAttachmentTxTypeParser()),
            Map.of(IndependentAttachmentType.NO_FEE, new NoFeeAttachmentParser())
    );

    @Test
    void parsePaymentWithMessageAndNoFee() {
        Wallet wallet = CryptoUtils.generateEthWallet();
        EncryptedData encryptedData = new EncryptedData(new byte[64], new byte[32]);
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 2, 100)
                .recipient(wallet.getAccount())
                .noFee(new NoFeeAttachment((byte) 1, 20))
                .message(new MessageAttachment((byte) 1, encryptedData, true, false))
                .amount(100)
                .data(new byte[1]);
        Transaction tx = builder.build(false);
        Transaction transaction = transactionParser.parseTx(tx.bytes(false));
        assertEquals(tx.getTxId(), transaction.getTxId());
        assertEquals(20, transaction.getAttachment(NoFeeAttachment.class).getPayer());
        assertEquals(encryptedData, transaction.getAttachment(MessageAttachment.class).getEncryptedData());
        assertNotNull(transaction.getAttachment(PaymentAttachment.class));
    }
}