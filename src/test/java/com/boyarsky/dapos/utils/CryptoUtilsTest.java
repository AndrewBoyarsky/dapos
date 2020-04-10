package com.boyarsky.dapos.utils;

import com.boyarsky.dapos.core.account.Wallet;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    @Test
    void testSignVerify() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate().getEncoded(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic().getEncoded(), "Text to sign".getBytes());
        assertTrue(verified);
    }

    @Test
    void testSignVerify_changed_message() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
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

    @Test
    void testCompressPublicKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        PublicKey aPublic = keyPair.getPublic();
        byte[] encoded = aPublic.getEncoded();
        System.out.println(Convert.toHexString(encoded));
        byte[] compressed = CryptoUtils.compress(keyPair.getPublic());
        assertEquals(33, compressed.length);
//        304d300706052b8104000a03420004be6b70d2a334640b2e54c060aaa2272061bdaaaa858f5121f98e2a2148e6820e84200f37a013a8e7e4bf156b31727cdb205feabfe8cd862f2e8cef4a28ab808c
//        304d300706052b8104000a03420004170f71f65d6fd8094bae2498cbfb1409fe810d5ac2916dc81b9a1f6448086cb326bcb22d8da00b7043e526a981ff968e1753be4cadc8b6706c2677d6d87c2f90
        PublicKey key = CryptoUtils.uncompress(compressed);

        assertArrayEquals(keyPair.getPublic().getEncoded(), key.getEncoded());

        KeyPair ed25519 = CryptoUtils.ed25519KeyPair();
        byte[] compressEd = CryptoUtils.compress(ed25519.getPublic());
        assertEquals(32, compressEd.length);

        PublicKey uncompress = CryptoUtils.uncompress(compressEd);

        assertArrayEquals(ed25519.getPublic().getEncoded(), uncompress.getEncoded());


    }

    @Test
    void testEncryptDecryptECDHDifferentCurves() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair sender = CryptoUtils.ed25519KeyPair();
        KeyPair rec = CryptoUtils.ed25519KeyPair();
        EncryptedData encryptedData = CryptoUtils.encryptX25519(sender.getPrivate().getEncoded(), rec.getPublic().getEncoded(), "123".getBytes());
        byte[] data = CryptoUtils.decryptX25519(rec.getPrivate().getEncoded(), sender.getPublic().getEncoded(), encryptedData);
        assertEquals("123".getBytes(), data);
    }
}