package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.Wallet;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    @Test
    void testCreate_pubKeyBitcoin() throws SignatureException, InvalidKeyException {
        Wallet senderWallet = CryptoUtils.generateBitcoinWallet();
        Wallet recipientWallet = CryptoUtils.generateBitcoinWallet();
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, senderWallet.getAccount(), senderWallet.getKeyPair(), 0);
        byte[] emptyData = new byte[20];
        Transaction tx = builder.amount(100)
                .recipient(recipientWallet.getAccount())
                .data(emptyData)
                .build(true);
        assertEquals(5, tx.getVersion());
        assertTrue(tx.isBitcoin());
        assertFalse(tx.isEd());
        assertTrue(tx.isFirst());
        boolean verified = CryptoUtils.verifySignature(tx.getSignature(), senderWallet.getKeyPair().getPublic(), tx.bytes(true));
        assertTrue(verified);
        byte[] bytes = tx.bytes(false);
        Transaction recreatedTx = new Transaction(bytes);
        assertEquals(tx.getTxId(), recreatedTx.getTxId());
        assertArrayEquals(tx.getSignature(), recreatedTx.getSignature());
    }

}