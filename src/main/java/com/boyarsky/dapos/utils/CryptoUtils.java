package com.boyarsky.dapos.utils;

import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.Wallet;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.EdEC;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
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
            KeyPair keyPair = secp256k1KeyPair();
            MessageDigest sha256 = keccak256();
            byte[] publicKeyHash = sha256.digest(keyPair.getPublic().getEncoded());
            byte[] address = new byte[20];
            System.arraycopy(publicKeyHash, 12, address, 0, 20);
            return new Wallet(new AccountId(encodeEthAddress(address)), keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
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

    public static KeyPair secp256k1KeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
        g.initialize(spec);
        return g.generateKeyPair();
    }

    public static KeyPair ed25519KeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("Ed25519", "BC");
        return g.generateKeyPair();
    }

    public static Wallet generateBitcoinWallet() {
        try {
            KeyPair keyPair = secp256k1KeyPair();
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
            return new Wallet(new AccountId(encodeBitcoinAddress(addressBytes)), keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] compress(PublicKey publicKey) {
        if (publicKey.getAlgorithm().equalsIgnoreCase("Ed25519")) {
            byte[] encoded = publicKey.getEncoded();
            byte[] compressed = new byte[32];
            System.arraycopy(encoded, 12, compressed, 0, 32);
            return compressed;
        } else if (publicKey.getAlgorithm().equalsIgnoreCase("EC")) {
            BCECPublicKey key = (BCECPublicKey) publicKey;
            return key.getQ().getEncoded(true);
        } else {
            throw new RuntimeException("Unable to compress " + publicKey);
        }
    }

    public static PublicKey uncompress(byte[] key) throws IOException {
        if (key.length == 33) {
            byte[] point = ECNamedCurveTable.getParameterSpec("secp256k1").getCurve().decodePoint(key).getEncoded(false);
            ASN1ObjectIdentifier secp256k1 = ECUtil.getNamedCurveOid("secp256k1");
            SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ECParameters.id_ecPublicKey, secp256k1), point);
            byte[] derEncoded = publicKeyInfo.getEncoded();
            System.out.println(Convert.toHexString(derEncoded));
            return getPublicKey(derEncoded, false);
        }
        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), key);
        return getPublicKey(subjectPublicKeyInfo.getEncoded(), false);
    }

    public static EncryptedData encryptECDH(byte[] privKey, byte[] pubKey, byte[] message) {
        return encryptWithKeyAgreement("ECDH", privKey, pubKey, message);
    }

    private static EncryptedData encryptWithKeyAgreement(String algorithm, byte[] privKey, byte[] pubKey, byte[] message) {
        PrivateKey privateKey = getPrivateKey(privKey, true);
        PublicKey publicKey = getPublicKey(pubKey, true);
        try {
            KeyAgreement agreement = KeyAgreement.getInstance(algorithm, "BC");
            agreement.init(privateKey);
            agreement.doPhase(publicKey, true);

            byte[] secret = agreement.generateSecret();
            byte[] nonce = generateBytes(32);
            byte[] secretKeyBytes = hmac(secret, nonce);
            return new EncryptedData(encryptAes(message, secretKeyBytes), nonce);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    public static EncryptedData encryptX25519(byte[] privKey, byte[] pubKey, byte[] message) {
        return encryptWithKeyAgreement("X25519", privKey, pubKey, message);
    }


    public static byte[] decryptECDH(byte[] privKey, byte[] pubKey, EncryptedData data) {
        return decryptWithKeyAgreement("ECDH", privKey, pubKey, data);
    }
    public static byte[] decryptX25519(byte[] privKey, byte[] pubKey, EncryptedData data) {
        return decryptWithKeyAgreement("X25519", privKey, pubKey, data);
    }

    private static byte[] decryptWithKeyAgreement(String algorithm, byte[] privKey, byte[] pubKey, EncryptedData data) {
        PrivateKey privateKey = getPrivateKey(privKey, true);
        PublicKey publicKey = getPublicKey(pubKey, true);
        try {
            KeyAgreement agreement = KeyAgreement.getInstance(algorithm, "BC");
            agreement.init(privateKey);
            agreement.doPhase(publicKey, true);
            byte[] secret = agreement.generateSecret();
            byte[] nonce = data.getNonce();
            byte[] secretKeyBytes = hmac(secret, nonce);
            return decryptAes(data.getEncrypted(), secretKeyBytes);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hmac(byte[] key, byte[] message) {
        try {
            Mac mac  = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKey);
            return mac.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey getPublicKey(byte[] key, boolean keyAgreement) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
        try {
            KeyFactory generator = KeyFactory.getInstance("EC", "BC");
            return generator.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            if (keyAgreement) {
                try {
                    KeyFactory factory = KeyFactory.getInstance("X25519", "BC");
                    var x509KeySpec = new X509EncodedKeySpec(key);
                    return factory.generatePublic(x509KeySpec);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                KeyFactory factory = KeyFactory.getInstance("Ed25519", "BC");
                var x509KeySpec = new X509EncodedKeySpec(key);
                return factory.generatePublic(x509KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static byte[] decodeBitcoinAddress(String address) {
        return Base58.decode(address);
    }

    public static String encodeBitcoinAddress(byte[] bitcoinAddressBytes) {
        return Base58.encode(bitcoinAddressBytes);
    }


    public static byte[] decodeEthAddress(String address) {
        return Convert.parseHexString(address.substring(2));
    }

    public static String encodeEthAddress(byte[] ethAddressBytes) {
        return "0x" + Convert.toHexString(ethAddressBytes);
    }

    public static PrivateKey getPrivateKey(byte[] key, boolean keyAgreement) {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory factory = KeyFactory.getInstance("EC", "BC");
            return factory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            if (keyAgreement) {
                try {
                    KeyFactory factory = KeyFactory.getInstance("X25519", "BC");
                    byte[] keyItself = new byte[32];
                    System.arraycopy(key, 16, keyItself, 0, 32);
                    return factory.generatePrivate(new SecretKeySpec(keyItself, "X25519"));
                } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                KeyFactory factory = KeyFactory.getInstance("Ed25519", "BC");
                return factory.generatePrivate(spec);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException ex) {
                throw new RuntimeException(ex);
            }
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
