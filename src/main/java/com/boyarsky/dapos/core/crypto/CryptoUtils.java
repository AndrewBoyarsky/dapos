package com.boyarsky.dapos.core.crypto;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.utils.Base58;
import com.boyarsky.dapos.utils.Convert;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
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
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.GZIPOutputStream;

public class CryptoUtils {
    public static final String PROVIDER = "BC";
    private static LazySodiumJava lazySodiumJava = new LazySodiumJava(new SodiumJava());
    private static final String ed25519 = "Ed25519";
    private static final String ec = "EC";
    private static final String secp256k1 = "secp256k1";

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

    public static Wallet generateEthWallet() {
        try {
            KeyPair keyPair = secp256k1KeyPair();
            return new Wallet(new AccountId(ethAddress(compress(keyPair.getPublic()))), keyPair);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ethAddress(byte[] publicKey) {
        MessageDigest sha256 = keccak256();
        byte[] publicKeyHash = sha256.digest(publicKey);
        byte[] address = new byte[20];
        System.arraycopy(publicKeyHash, 12, address, 0, 20);
        return encodeEthAddress(address);
    }

    public static KeyPair getKeyPair(String crypto, byte[] privKey, byte[] pubKey) throws InvalidKeySpecException {
        return new KeyPair(getPublicKey(crypto, pubKey), getPrivateKey(crypto, privKey));
    }

    public static Wallet generateEd25Wallet() {
        try {
            KeyPair keyPair = ed25519KeyPair();
            return new Wallet(new AccountId(ed25Address(compress(keyPair.getPublic()))), keyPair);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static Wallet generateValidatorWallet() {
        try {
            KeyPair keyPair = ed25519KeyPair();
            return new Wallet(new AccountId(validatorAddress(compress(keyPair.getPublic()))), keyPair);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ed25Address(byte[] publicKey) {
        MessageDigest digest = keccak256();
        byte[] publicKeyHash = digest.digest(publicKey);
        byte[] address = new byte[16];
        System.arraycopy(publicKeyHash, 0, address, 0, 16);
        return encodeEd25Address(address);
    }

    public static boolean isCompatible(AccountId id1, AccountId id2) {
        return isEd25(id1) && isEd25(id2) || isSecp(id1) && isSecp(id2);
    }

    public static byte[] compress(byte[] bytes) throws IOException {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             GZIPOutputStream gzipStream = new GZIPOutputStream(os)) {
            gzipStream.write(bytes);
            return os.toByteArray();
        }
    }

    public static boolean isEd25(AccountId id) {
        return id.isVal() || id.isEd25();
    }

    public static boolean isSecp(AccountId id) {
        return id.isEth() || id.isBitcoin();
    }

    public static String validatorAddress(byte[] publicKey) {
        MessageDigest sha256 = sha256();
        byte[] publicKeyHash = sha256.digest(publicKey);
        byte[] address = new byte[20];
        System.arraycopy(publicKeyHash, 0, address, 0, 20);
        return encodeValidatorAddress(address);
    }

    public static boolean verifySignature(byte[] signature, PublicKey publicKey, byte[] message) throws InvalidKeyException, SignatureException {
        return verifySignature(publicKey.getAlgorithm(), signature, publicKey, message);
    }

    public static boolean verifySignature(boolean isEd, byte[] signature, PublicKey publicKey, byte[] message) throws InvalidKeyException, SignatureException {
        return verifySignature(resolveCrypto(isEd), signature, publicKey, message);
    }

    private static String resolveCrypto(boolean isEd) {
        return isEd ? ed25519 : ec;
    }

    public static boolean verifySignature(String crypto, byte[] signature, PublicKey publicKey, byte[] message) throws InvalidKeyException, SignatureException {
        try {
            Signature instance = createSignature(crypto);
            instance.initVerify(publicKey);
            instance.update(message);
            return instance.verify(signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("FATAL ERROR. BC is not registered properly.", e);
        }
    }

    public static byte[] sign(PrivateKey key, byte[] message) {
        try {
            Signature instance = createSignature(key.getAlgorithm());
            instance.initSign(key);
            instance.update(message);
            return instance.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] compressSignature(byte[] signature) {
        if (signature.length == 64) {
            return signature;
        }
        byte[] compressed = new byte[64];
        if (signature[3] == 32) {
            System.arraycopy(signature, 4, compressed, 0, 32);
        } else {
            System.arraycopy(signature, 5, compressed, 0, 32);
        }
        int pos = 3 + signature[3] + 2;
        if (signature[pos] == 32) {
            System.arraycopy(signature, pos + 1, compressed, 32, 32);
        } else {
            System.arraycopy(signature, pos + 2, compressed, 32, 32);
        }
        return compressed;
    }

    public static byte[] uncompressSignature(byte[] signature) {
        int additionalSize = 0;
        if (signature[0] < 0) {
            additionalSize++;
        }
        if (signature[32] < 0) {
            additionalSize++;
        }
        byte[] uncompressed = new byte[70 + additionalSize];
        uncompressed[0] = 48;
        uncompressed[1] = (byte) (uncompressed.length - 2);
        uncompressed[2] = 2;
        int firstStartPos;
        if (signature[0] < 0) {
            uncompressed[3] = 33;
            firstStartPos = 5;
        } else {
            uncompressed[3] = 32;
            firstStartPos = 4;
        }
        System.arraycopy(signature, 0, uncompressed, firstStartPos, 32);
        int secondPos = firstStartPos + 32;
        uncompressed[secondPos] = 2;
        if (signature[32] < 0) {
            uncompressed[++secondPos] = 33;
            ++secondPos;
        } else {
            uncompressed[++secondPos] = 32;
        }
        secondPos++;
        System.arraycopy(signature, 32, uncompressed, secondPos, 32);
        return uncompressed;
    }

    private static Signature createSignature(String algo) throws NoSuchProviderException, NoSuchAlgorithmException {
        if (algo.equals(ec)) {
            return Signature.getInstance("SHA256withECDSA", PROVIDER);
        } else if (algo.equals(ed25519)) {
            return Signature.getInstance(ed25519, PROVIDER);
        } else {
            throw new RuntimeException("Unsupported key algo: " + algo);
        }
    }

    public static KeyPair secp256k1KeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance(ec, PROVIDER);
        ECGenParameterSpec spec = new ECGenParameterSpec(secp256k1);
        g.initialize(spec);
        return g.generateKeyPair();
    }

    public static KeyPair ed25519KeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance(ed25519, PROVIDER);
        return g.generateKeyPair();
    }

    public static KeyPair x25519KeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("X25519", PROVIDER);
        return g.generateKeyPair();
    }

    public static Wallet generateBitcoinWallet() {
        try {
            KeyPair keyPair = secp256k1KeyPair();
            String address = bitcoinAddress(compress(keyPair.getPublic()));
            return new Wallet(new AccountId(address), keyPair);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bitcoinAddress(byte[] publicKey) {
        MessageDigest sha256 = sha256();
        MessageDigest ripeMd = ripemd160();
        byte[] sha256PublicKey = sha256.digest(publicKey);
        byte[] ripeMdSha256 = ripeMd.digest(sha256PublicKey);
        byte[] ripeWithVersion = new byte[21];
        System.arraycopy(ripeMdSha256, 0, ripeWithVersion, 1, ripeMdSha256.length);
        ripeWithVersion[0] = 0; // mainnet
        byte[] firstSHA256 = sha256.digest(ripeWithVersion);
        byte[] secondSHA256 = sha256.digest(firstSHA256);
        byte[] addressBytes = new byte[25];
        System.arraycopy(ripeWithVersion, 0, addressBytes, 0, ripeWithVersion.length);
        System.arraycopy(secondSHA256, 0, addressBytes, ripeWithVersion.length, 4);
        return encodeBitcoinAddress(addressBytes);
    }

    public static byte[] compress(PublicKey publicKey) {
        if (publicKey.getAlgorithm().equalsIgnoreCase(ed25519)) {
            byte[] encoded = publicKey.getEncoded();
            byte[] compressed = new byte[32];
            System.arraycopy(encoded, 12, compressed, 0, 32);
            return compressed;
        } else if (publicKey.getAlgorithm().equalsIgnoreCase(ec)) {
            BCECPublicKey key = (BCECPublicKey) publicKey;
            return key.getQ().getEncoded(true);
        } else {
            throw new RuntimeException("Unable to compress public key " + publicKey);
        }
    }


    public static byte[] compress(PrivateKey privateKey) {
        if (privateKey.getAlgorithm().equalsIgnoreCase(ed25519)) {
            byte[] encoded = privateKey.getEncoded();
            byte[] compressed = new byte[32];
            System.arraycopy(encoded, 16, compressed, 0, 32);
            return compressed;
        } else if (privateKey.getAlgorithm().equalsIgnoreCase(ec)) {
            BCECPrivateKey key = (BCECPrivateKey) privateKey;
            return key.getS().toByteArray();
        } else {
            throw new RuntimeException("Unable to compress private key " + privateKey);
        }
    }

    public static PrivateKey toX25519(PrivateKey ed25519) {
        byte[] compressed = compress(ed25519);
        byte[] x25519Raw = toX25519Priv(compressed);
        try {
            KeyFactory factory = KeyFactory.getInstance("X25519", PROVIDER);
            return factory.generatePrivate(new PKCS8EncodedKeySpec(new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519), new DEROctetString(x25519Raw)).getEncoded()));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey toX25519(PublicKey ed25519) {
        byte[] compressed = compress(ed25519);
        try {
            byte[] x25519Raw = toX25519(compressed);
            KeyFactory factory = KeyFactory.getInstance("X25519", PROVIDER);
            return factory.generatePublic(new X509EncodedKeySpec(new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519), x25519Raw).getEncoded()));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toX25519Priv(byte[] ed25519PrivKey) {
        byte[] x25519Priv = new byte[32];
        lazySodiumJava.convertSecretKeyEd25519ToCurve25519(x25519Priv, ed25519PrivKey);
        return x25519Priv;
    }

    public static byte[] uncompress(String crypto, byte[] key) throws InvalidKeyException {
        try {
            if (isSecp256k1(crypto)) {
                if (key.length != 33) {
                    throw new InvalidKeyException("Secp256k1 compressed key should be of 33 length got: " + Convert.toHexString(key));
                }
                byte[] point = ECNamedCurveTable.getParameterSpec(secp256k1).getCurve().decodePoint(key).getEncoded(false);
                ASN1ObjectIdentifier identifier = ECUtil.getNamedCurveOid(secp256k1);
                SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ECParameters.id_ecPublicKey, identifier), point);
                return publicKeyInfo.getEncoded();
            } else if (isEd25519(crypto)) {
                if (key.length != 32) {
                    throw new InvalidKeyException("Ed25519 compressed key should be of 32 length, got: " + Convert.toHexString(key));
                }
                SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), key);
                return subjectPublicKeyInfo.getEncoded();
            } else {
                throw new InvalidKeyException("Unknown key format: " + crypto);
            }
        } catch (IOException e) {
            // invalid encoding, should never happens
            throw new RuntimeException("Invalid key encoding: " + Convert.toHexString(key));
        }
    }

    private static boolean isSecp256k1(String crypto) {
        return ec.equalsIgnoreCase(crypto);
    }

    private static boolean isEd25519(String crypto) {
        return ed25519.equalsIgnoreCase(crypto);
    }

    public static AccountId fromPublicKey(byte[] pubKey, boolean isBitcoin, boolean isEd) {
        if (pubKey.length == 33) {
            if (isBitcoin) {
                return new AccountId(bitcoinAddress(pubKey));
            } else {
                return new AccountId(ethAddress(pubKey));
            }
        } else if (pubKey.length == 32) {
            if (isEd) {
                return new AccountId(ed25Address(pubKey));
            } else {
                return new AccountId(validatorAddress(pubKey));
            }
        } else {
            throw new RuntimeException("Invalid pub key");
        }
    }

    public static byte[] toX25519(byte[] ed25519PubKey) throws IOException {
        if (ed25519PubKey.length == 32) {
            byte[] x25519 = new byte[32];
            lazySodiumJava.convertPublicKeyEd25519ToCurve25519(x25519, ed25519PubKey);
            return x25519;
        } else {
            throw new RuntimeException("Unknown key format");
        }
    }

    public static EncryptedData encryptECDH(PrivateKey privateKey, PublicKey publicKey, byte[] message) {
        return encryptWithKeyAgreement("ECDH", privateKey, publicKey, message);
    }

    private static EncryptedData encryptWithKeyAgreement(String algorithm, PrivateKey privKey, PublicKey pubKey, byte[] message) {
        try {
            KeyAgreement agreement = KeyAgreement.getInstance(algorithm, PROVIDER);
            agreement.init(privKey);
            agreement.doPhase(pubKey, true);

            byte[] secret = agreement.generateSecret();
            byte[] nonce = generateBytes(32);
            byte[] secretKeyBytes = hmac(secret, nonce);
            return new EncryptedData(encryptAes(message, secretKeyBytes), nonce);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    public static EncryptedData encryptX25519(PrivateKey privateKey, PublicKey publicKey, byte[] message) {
        return encryptWithKeyAgreement("X25519", privateKey, publicKey, message);
    }

    public static EncryptedData encryptX25519WithEd25519(PrivateKey privateKey, PublicKey publicKey, byte[] message) {
        return encryptWithKeyAgreement("X25519", toX25519(privateKey), toX25519(publicKey), message);
    }


    public static byte[] decryptECDH(PrivateKey privKey, PublicKey pubKey, EncryptedData data) {
        return decryptWithKeyAgreement("ECDH", privKey, pubKey, data);
    }

    public static byte[] decryptX25519(PrivateKey privKey, PublicKey pubKey, EncryptedData data) {
        return decryptWithKeyAgreement("X25519", privKey, pubKey, data);
    }

    private static byte[] decryptWithKeyAgreement(String algorithm, PrivateKey privKey, PublicKey pubKey, EncryptedData data) {
        try {
            KeyAgreement agreement = KeyAgreement.getInstance(algorithm, PROVIDER);
            agreement.init(privKey);
            agreement.doPhase(pubKey, true);
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

    public static PublicKey getPublicKey(String crypto, byte[] key) throws InvalidKeySpecException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
        try {
            KeyFactory generator = KeyFactory.getInstance(crypto, PROVIDER);
            return generator.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey getUncompressedPublicKey(byte[] publicKey) throws InvalidKeyException {
        String keySpec = publicKey.length == 32 ? ed25519 : ec;
        return getUncompressedPublicKey(keySpec, publicKey);
    }

    public static PublicKey getUncompressedPublicKey(boolean isEd, byte[] publicKey) throws InvalidKeyException {
        String keySpec = isEd ? ed25519 : ec;
        return getUncompressedPublicKey(keySpec, publicKey);
    }

    public static PublicKey getUncompressedPublicKey(String crypto, byte[] publicKey) throws InvalidKeyException {
        byte[] uncompress = uncompress(crypto, publicKey);
        try {
            return getPublicKey(crypto, uncompress);
        } catch (InvalidKeySpecException e) { // should never happens
            throw new RuntimeException("FATAL ERROR! Incorrect public key spec: received - " + crypto, e);
        }
    }

    public static byte[] decodeBitcoinAddress(String address) {
        return Base58.decode(address);
    }

    public static String encodeBitcoinAddress(byte[] bitcoinAddressBytes) {
        return Base58.encode(bitcoinAddressBytes);
    }

    public static String encodeValidatorAddress(byte[] addressBytes) {
        return "nn" + Convert.toHexString(addressBytes);
    }

    public static byte[] decodeValidatorAddress(String address) {
        return Convert.parseHexString(address.substring(2));
    }


    public static byte[] decodeEthAddress(String address) {
        return Convert.parseHexString(address.substring(2));
    }

    public static byte[] decodeEd25Address(String address) {
        return Convert.parseHexString(address.substring(2));
    }

    public static String encodeEthAddress(byte[] ethAddressBytes) {
        return "0x" + Convert.toHexString(ethAddressBytes);
    }
    public static String encodeEd25Address(byte[] ethAddressBytes) {
        return "25" + Convert.toHexString(ethAddressBytes);
    }

    public static PrivateKey getPrivateKey(String crypto, byte[] key) {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory factory = KeyFactory.getInstance(crypto, PROVIDER);
            return factory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
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
