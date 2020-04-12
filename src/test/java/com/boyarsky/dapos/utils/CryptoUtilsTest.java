package com.boyarsky.dapos.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    @Test
    void testSignVerifySecp256k1() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        testSignVerify(keyPair);
    }


    @Test
    void testSignVerify_changed_message() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic(), "Text to sig".getBytes());
        assertFalse(verified);
    }

    @Test
    void testSignVerify_ed25519() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.ed25519KeyPair();
        testSignVerify(keyPair);
    }

    private void testSignVerify(KeyPair keyPair) throws SignatureException, InvalidKeyException {
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate(), "Text to sign".getBytes());
        System.out.println(Convert.toHexString(signature));
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic(), "Text to sign".getBytes());
        assertTrue(verified);
    }

    @Test
    void testEncryptDecryptECDH() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair sender = CryptoUtils.secp256k1KeyPair();
        KeyPair recipient = CryptoUtils.secp256k1KeyPair();
        KeyPair intruder = CryptoUtils.secp256k1KeyPair();
        String secretMessage = "encrypted message";
        EncryptedData encryptedData = CryptoUtils.encryptECDH(sender.getPrivate(), recipient.getPublic(), secretMessage.getBytes());
        byte[] bytes = CryptoUtils.decryptECDH(recipient.getPrivate(), sender.getPublic(), encryptedData);
        String decrypted = new String(bytes);
        assertEquals(secretMessage, decrypted);

        assertThrows(RuntimeException.class, () -> CryptoUtils.decryptECDH(intruder.getPrivate(), sender.getPublic(), encryptedData));
    }

    @Test
    void testCompressPublicKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        byte[] compressed = CryptoUtils.compress(keyPair.getPublic());
        assertEquals(33, compressed.length);
        PublicKey key = CryptoUtils.getPublicKey("EC", CryptoUtils.uncompress(compressed));

        assertArrayEquals(keyPair.getPublic().getEncoded(), key.getEncoded());

        KeyPair ed25519 = CryptoUtils.ed25519KeyPair();
        byte[] compressEd = CryptoUtils.compress(ed25519.getPublic());
        assertEquals(32, compressEd.length);

        PublicKey uncompress = CryptoUtils.getPublicKey("Ed25519", CryptoUtils.uncompress(compressEd));

        assertArrayEquals(ed25519.getPublic().getEncoded(), uncompress.getEncoded());
    }

    @Test
    void testX25519() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPair recipient = CryptoUtils.ed25519KeyPair();
        PublicKey x25519RecipientPublic = CryptoUtils.toX25519(recipient.getPublic());
        PrivateKey x25519RecipientPrivKey = CryptoUtils.toX25519(recipient.getPrivate());

        KeyPair sender = CryptoUtils.ed25519KeyPair();
        PublicKey x25519SenderPublic = CryptoUtils.toX25519(sender.getPublic());
        PrivateKey x25519SenderPrivKey = CryptoUtils.toX25519(sender.getPrivate());


        String toDecrypt = "data data";
        byte[] data = toDecrypt.getBytes();
        EncryptedData encryptedData = CryptoUtils.encryptX25519(x25519SenderPrivKey, x25519RecipientPublic, data);
        byte[] bytes = CryptoUtils.decryptX25519(x25519RecipientPrivKey, x25519SenderPublic, encryptedData);

        assertEquals(toDecrypt, new String(bytes));
    }
}