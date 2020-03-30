package com.boyarsky.dapos;

import com.boyarsky.dapos.utils.CryptoUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptoUtilsTest {
    {
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
}
