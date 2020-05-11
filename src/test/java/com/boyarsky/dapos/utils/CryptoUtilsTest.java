package com.boyarsky.dapos.utils;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.RepeatedTest;
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
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @RepeatedTest(value = 1000)
    void testSignVerifySecp256k1() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        testSignVerify(keyPair);
    }

    @Test
    void testCompressUncompressBytes() throws IOException {
        byte[] bytes = "Text to compress gzip".getBytes();
        byte[] compress = CryptoUtils.compress(bytes);
        byte[] uncompress = CryptoUtils.uncompress(compress);
        assertArrayEquals(bytes, uncompress);
    }

    @Test
    void testCompressUncompressSig() throws InvalidKeyException {
        testSignatureCompressionUncompression("304502203d694333a4119c5e27a40dae6c050753a76d62e2eeb5e9508229e2fa348aaf16022100bd67c5e0d2f5763d90b071e365a6ff4f756208fc56b393c1a935ba192d11f3e8");
        testSignatureCompressionUncompression("3046022100bf694333a4119c5e27a40dae6c050753a76d62e2eeb5e9508229e2fa348aaf16022100bd67c5e0d2f5763d90b071e365a6ff4f756208fc56b393c1a935ba192d11f3e8");
        testSignatureCompressionUncompression("3045022100bf694333a4119c5e27a40dae6c050753a76d62e2eeb5e9508229e2fa348aaf1602204867c5e0d2f5763d90b071e365a6ff4f756208fc56b393c1a935ba192d11f3e8");
        testSignatureCompressionUncompression("3044022021694333a4119c5e27a40dae6c050753a76d62e2eeb5e9508229e2fa348aaf1602204867c5e0d2f5763d90b071e365a6ff4f756208fc56b393c1a935ba192d11f3e8");
        testSignatureCompressionUncompression("30440220008221b1250bfc320bd6fc271bc31e59fc6a0f9c1cc62298faec6b8b86f20565022009cd82adb1664d297f7299369764eef8a13266bf0ba26f9b34644ec5f7e19d50");
        testSignatureCompressionUncompression("30410220008221b1250bfc320bd6fc271bc31e59fc6a0f9c1cc62298faec6b8b86f20565021d09b1664d297f7299369764eef8a13266bf0ba26f9b34644ec5f7e19d50");
    }

    void testSignatureCompressionUncompression(String sig) {
        byte[] sigBytes = Convert.parseHexString(sig);
        byte[] compressed = CryptoUtils.compressSignature(sigBytes);
        byte[] uncompressed = CryptoUtils.uncompressSignature(compressed);
        assertEquals(Convert.toHexString(sigBytes), Convert.toHexString(uncompressed));
    }

    @Test
    void testSignVerify_changed_message() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic(), "Text to sig".getBytes());
        assertFalse(verified);
    }

    @RepeatedTest(1000)
    void testSignVerify_ed25519() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        KeyPair keyPair = CryptoUtils.ed25519KeyPair();
        testSignVerify(keyPair);
    }

    private void testSignVerify(KeyPair keyPair) throws SignatureException, InvalidKeyException {
        byte[] signature = CryptoUtils.sign(keyPair.getPrivate(), "Text to sign".getBytes());
        boolean verified = CryptoUtils.verifySignature(signature, keyPair.getPublic(), "Text to sign".getBytes());
        assertTrue(verified);
        byte[] compressed = CryptoUtils.compressSignature(signature);
        assertEquals(64, compressed.length);
        byte[] restored = signature.length > 64 ? CryptoUtils.uncompressSignature(compressed) : compressed;
        assertArrayEquals(signature, restored, Convert.toHexString(signature) + "\n" + Convert.toHexString(restored));
        boolean verifiedRestored = CryptoUtils.verifySignature(restored, keyPair.getPublic(), "Text to sign".getBytes());
        assertTrue(verifiedRestored);
    }

    @Test
    void createValidatorWallet() {
        Wallet wallet = CryptoUtils.generateValidatorWallet();
        assertEquals("Ed25519", wallet.getKeyPair().getPrivate().getAlgorithm());
        assertTrue(wallet.getAccount().isVal());
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
    void testCompressPublicKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeyException, InvalidKeySpecException {
        KeyPair keyPair = CryptoUtils.secp256k1KeyPair();
        byte[] compressed = CryptoUtils.compress(keyPair.getPublic());
        assertEquals(33, compressed.length);
        PublicKey key = CryptoUtils.getPublicKey("EC", CryptoUtils.uncompress("EC", compressed));

        assertArrayEquals(keyPair.getPublic().getEncoded(), key.getEncoded());

        KeyPair ed25519 = CryptoUtils.ed25519KeyPair();
        byte[] compressEd = CryptoUtils.compress(ed25519.getPublic());
        assertEquals(32, compressEd.length);

        PublicKey uncompress = CryptoUtils.getPublicKey("Ed25519", CryptoUtils.uncompress("Ed25519", compressEd));

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