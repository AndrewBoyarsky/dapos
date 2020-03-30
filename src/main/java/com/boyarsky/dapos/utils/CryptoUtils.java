package com.boyarsky.dapos.utils;

import com.boyarsky.dapos.account.Wallet;
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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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

//    public static ECPublicKey getPublicKey(byte[] publicKeyBytes) {
//        // First we separate x and y of coordinates into separate variables
//        byte[] x = new byte[32];
//        byte[] y = new byte[32];
//        System.arraycopy(publicKeyBytes, 1, x, 0, 32);
//        System.arraycopy(publicKeyBytes, 33, y, 0, 32);
//
//        try {
//            KeyFactory kf = KeyFactory.getInstance("EC");
//
//            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
//            parameters.init(new ECGenParameterSpec("secp256k1"));
//            java.security.spec.ECParameterSpec ecParameterSpec = parameters.getParameterSpec(java.security.spec.ECParameterSpec.class);
//
//            ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(x), new BigInteger(y)), ecParameterSpec);
//            ECPublicKey ecPublicKey = (ECPublicKey) kf.generatePublic(ecPublicKeySpec);
//            return ecPublicKey;
//        } catch (NoSuchAlgorithmException | InvalidParameterSpecException | InvalidKeySpecException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    public static ECPublicKey getPublicKey(BigInteger key) {
//        try {
//            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
//            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//            if (key.bitLength() > ecSpec.getN().bitLength()) {
//                System.out.println("Trim key " + key);
//                key = key.mod(ecSpec.getN());
//            }
//            ECPoint Q = ecSpec.getG().multiply(key.abs());
//
//            ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
//            return (ECPublicKey) keyFactory.generatePublic(pubSpec);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static Wallet generateEthWallet() {
        try {
            KeyPair keyPair = generateKeyPair();
            MessageDigest sha256 = keccak256();
            byte[] publicKeyHash = sha256.digest(keyPair.getPublic().getEncoded());
            byte[] address = new byte[20];
            System.arraycopy(publicKeyHash, 12, address, 0, 20);
            return new Wallet("0x" + Convert.toHexString(address), keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(byte[] signature, byte[] publicKey, byte[] message) {
        try {
            Signature instance = Signature.getInstance("SHA256withECDSA", "BC");
            instance.initVerify(KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKey)));
            instance.update(message);
            return instance.verify(signature);
        } catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sign(byte[] privKey, byte[] message) {
        try {
            Signature instance = Signature.getInstance("SHA256withECDSA", "BC");
            instance.initSign(KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(privKey)));
            instance.update(message);
            return instance.sign();
        } catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
        g.initialize(spec);
        return g.generateKeyPair();
    }

    public static Wallet generateBitcoinWallet() {
        try {
            KeyPair keyPair = generateKeyPair();
            MessageDigest sha256 = sha256();
            MessageDigest ripeMd = ripemd160();
            byte[] sha256PublicKey = sha256.digest(keyPair.getPublic().getEncoded());
            byte[] ripeMdSha256 = ripeMd.digest(sha256PublicKey);
            byte[] ripeWithVersion = new byte[21];
            System.arraycopy(ripeMdSha256, 0, ripeWithVersion, 1, ripeMdSha256.length);
            ripeWithVersion[0] = 0; // mainnet
            byte[] firstSHA256 = sha256.digest(ripeWithVersion);
            byte[] secondSHA256 = sha256.digest(firstSHA256);
            byte[] addressBytes = new byte[25];
            System.arraycopy(ripeWithVersion, 0, addressBytes, 0, ripeWithVersion.length);
            System.arraycopy(secondSHA256, 0, addressBytes, ripeWithVersion.length, 4);
            String encode = Base58.encode(addressBytes);
            return new Wallet(encode, keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

//    public static ECPrivateKey getPrivateKey(byte[] key) {
//        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
//        try {
//            KeyFactory factory = KeyFactory.getInstance("EC");
//            return (ECPrivateKey) factory.generatePrivate(spec);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
