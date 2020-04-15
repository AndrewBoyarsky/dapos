package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.Wallet;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {
    @Test
    void testCreate_pubKeyBitcoin() throws SignatureException, InvalidKeyException {
        Wallet senderWallet = CryptoUtils.generateBitcoinWallet();
        Wallet recipientWallet = CryptoUtils.generateBitcoinWallet();
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, senderWallet.getAccount(), senderWallet.getKeyPair(), 0, 0);
        byte[] emptyData = new byte[20];
        Transaction tx = builder.amount(100)
                .recipient(recipientWallet.getAccount())
                .data(emptyData)
                .build(true);
        assertEquals(5, tx.getVersion());
        assertTrue(tx.isBitcoin());
        assertFalse(tx.isEd());
        assertTrue(tx.isFirst());
        boolean verified = CryptoUtils.verifySignature(CryptoUtils.uncompressSignature(tx.getSignature()), senderWallet.getKeyPair().getPublic(), tx.bytes(true));
        assertTrue(verified);
        byte[] bytes = tx.bytes(false);
        Transaction recreatedTx = new Transaction(bytes);
        assertEquals(tx.getTxId(), recreatedTx.getTxId());
        assertArrayEquals(tx.getSignature(), recreatedTx.getSignature());
    }

    @Test
    void testCreate_senderAccountEd25_no_amount_no_recipient() throws SignatureException, InvalidKeyException {
        Wallet senderWallet = CryptoUtils.generateEd25Wallet();
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.SET_FEE_PROVIDER, senderWallet.getAccount(), senderWallet.getKeyPair(), 1, 2);
        byte[] someData = new byte[32];
        new Random().nextBytes(someData);
        Transaction tx = builder
                .data(someData)
                .build(false);
        assertEquals(2, tx.getVersion());
        assertFalse(tx.isBitcoin());
        assertTrue(tx.isEd());
        assertFalse(tx.isFirst());
        boolean verified = CryptoUtils.verifySignature(tx.getSignature(), senderWallet.getKeyPair().getPublic(), tx.bytes(true));
        assertTrue(verified);
        byte[] bytes = tx.bytes(false);
        Transaction recreatedTx = new Transaction(bytes);
        assertEquals(tx.getTxId(), recreatedTx.getTxId());
        assertArrayEquals(tx.getSignature(), recreatedTx.getSignature());
    }

    @Test
    void testCreate_firstPublicKeyEd25() throws SignatureException, InvalidKeyException {
        Wallet senderWallet = CryptoUtils.generateEd25Wallet();
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, senderWallet.getAccount(), senderWallet.getKeyPair(), 0, 2);
        byte[] someData = new byte[2];
        new Random().nextBytes(someData);
        Transaction tx = builder
                .amount(10)
                .data(someData)
                .build(true);
        assertEquals(3, tx.getVersion());
        assertFalse(tx.isBitcoin());
        assertTrue(tx.isEd());
        assertTrue(tx.isFirst());
        boolean verified = CryptoUtils.verifySignature(tx.getSignature(), senderWallet.getKeyPair().getPublic(), tx.bytes(true));
        assertTrue(verified);
        byte[] bytes = tx.bytes(false);
        Transaction recreatedTx = new Transaction(bytes);
        assertEquals(tx.getTxId(), recreatedTx.getTxId());
        assertArrayEquals(tx.getSignature(), recreatedTx.getSignature());
    }


    @Test
    void testCreate_senderAccountEth_no_data() throws SignatureException, InvalidKeyException {
        Wallet senderWallet = CryptoUtils.generateEthWallet();
        Wallet recipientWallet = CryptoUtils.generateBitcoinWallet();
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.SET_FEE_PROVIDER, senderWallet.getAccount(), senderWallet.getKeyPair(), 100, 200);
        Transaction tx = builder
                .recipient(recipientWallet.getAccount())
                .build(false);
        assertEquals(0, tx.getVersion());
        assertFalse(tx.isBitcoin());
        assertFalse(tx.isEd());
        assertFalse(tx.isFirst());
        boolean verified = CryptoUtils.verifySignature(CryptoUtils.uncompressSignature(tx.getSignature()), senderWallet.getKeyPair().getPublic(), tx.bytes(true));
        assertTrue(verified);
        byte[] bytes = tx.bytes(false);
        Transaction recreatedTx = new Transaction(bytes);
        assertEquals(tx.getTxId(), recreatedTx.getTxId());
        assertArrayEquals(tx.getSignature(), recreatedTx.getSignature());
    }


}