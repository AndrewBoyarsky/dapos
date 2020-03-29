package com.boyarsky.dapos.utils;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

public class CryptoUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageDigest keccak256() {
        try {
            return MessageDigest.getInstance("Keccak-256");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptAes(byte[] content, byte[] key) {
    byte[] iv = generateBytes(16);
        try {
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = aes.doFinal(content);
            byte[] result = new byte[encrypted.length + iv.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptAes(byte[] content, byte[] key) {
        try {
            byte[] iv = new byte[16];
            System.arraycopy(content, 0, iv, 0, 16);
            byte[] toDecrypt = new byte[content.length - 16];
            System.arraycopy(content, 16, toDecrypt, 0, toDecrypt.length);
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return aes.doFinal(toDecrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateBytes(int size) {
        byte[] nonce = new byte[size];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static MessageDigest ripemd160() {
        return new RIPEMD160.Digest();
    }
}
