package com.boyarsky.dapos.utils;

import com.boyarsky.dapos.core.account.Wallet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    @Test
    void testSignVerify() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = CryptoUtils.generateKeyPair();
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate().getEncoded(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic().getEncoded(), "Text to sign".getBytes());
        assertTrue(verified);
    }

    @Test
    void testSignVerify_changed_message() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = CryptoUtils.generateKeyPair();
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate().getEncoded(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic().getEncoded(), "Text to sig".getBytes());
        assertFalse(verified);
    }

    @Test
    void testEncryptDecryptECDH() {
        Wallet ethWallet = CryptoUtils.generateEthWallet();
        Wallet bitcoinWallet = CryptoUtils.generateBitcoinWallet();
        Wallet intruderWallet = CryptoUtils.generateBitcoinWallet();
        String secretMessage = "encrypted message";
        EncryptedData encryptedData = CryptoUtils.encryptECDH(ethWallet.getPrivateKey(), bitcoinWallet.getPublicKey(), secretMessage.getBytes());
        byte[] bytes = CryptoUtils.decryptECDH(bitcoinWallet.getPrivateKey(), ethWallet.getPublicKey(), encryptedData);
        String decrypted = new String(bytes);
        assertEquals(secretMessage, decrypted);

        assertThrows(RuntimeException.class, () -> CryptoUtils.decryptECDH(intruderWallet.getPrivateKey(), ethWallet.getPublicKey(), encryptedData));
    }
}