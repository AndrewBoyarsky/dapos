 /*
  * Copyright © 2018 Apollo Foundation
  */

 package com.boyarsky.dapos.account;

 import com.apollocurrency.aplwallet.apl.util.StringUtils;
 import com.boyarsky.dapos.utils.Base58;
 import com.boyarsky.dapos.utils.Convert;
 import com.boyarsky.dapos.utils.CryptoUtils;
 import com.boyarsky.dapos.core.TimeSource;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;

 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.ECGenParameterSpec;
 import java.time.Instant;
 import java.time.LocalDateTime;
 import java.time.ZoneId;
 import java.time.format.DateTimeFormatter;
 import java.util.Date;

 import static com.boyarsky.dapos.utils.CryptoUtils.keccak256;
 import static com.boyarsky.dapos.utils.CryptoUtils.ripemd160;
 import static com.boyarsky.dapos.utils.CryptoUtils.sha256;

 @Service
 @Slf4j
 public class KeyStoreService implements Keystore {
     private static final Integer CURRENT_KEYSTORE_VERSION = 1;
     private Path keystoreDirPath;
     private Integer version;
     private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
     private static final String FORMAT = "v%d_%s---%s";
     private TimeSource timeSource;
     private PassphraseGenerator generator;
     private static ObjectMapper mapper = new ObjectMapper();
     static {
         mapper.registerModule(new JavaTimeModule());
     }

     @Autowired
     public KeyStoreService(@Qualifier("keystoreDir") Path keystoreDir, TimeSource timeSource, PassphraseGenerator generator) {
         this(keystoreDir, CURRENT_KEYSTORE_VERSION, timeSource, generator);
     }

     public KeyStoreService(Path keystoreDir, Integer version, TimeSource ntpTime, PassphraseGenerator generator) {
         if (version < 0) {
             throw new IllegalArgumentException("version should not be negative");
         }
         this.version = version;
         this.keystoreDirPath = keystoreDir;
         this.timeSource = ntpTime;
         this.generator = generator;
         if (!Files.exists(keystoreDirPath)) {
             try {
                 Files.createDirectories(keystoreDirPath);
             } catch (IOException e) {
                 throw new RuntimeException(e.toString(), e);
             }
         }
     }

     @Override
     public PassphraseProtectedWallet createBitcoin(String pass) {
         Wallet wallet = generateBitcoinWallet();
         LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeSource.getTime()), ZoneId.systemDefault());
         String keyPath = String.format(FORMAT, version, FORMATTER.format(currentTime), wallet.getAppAccount());
         Path keyFile = keystoreDirPath.resolve(keyPath);
         if (Files.exists(keyFile)) {
             throw new RuntimeException("key file already exits. Should not happen");
         }
         if (StringUtils.isBlank(pass)) {
             pass = generator.generate();
         }
         byte[] encryptedPrivateKey = CryptoUtils.encryptAes(wallet.getPrivateKey(), sha256().digest(pass.getBytes()));
         StoredWallet storedWallet = new StoredWallet(wallet.getAccount(), Convert.toHexString(wallet.getPublicKey()), Convert.toHexString(encryptedPrivateKey), currentTime);
         try {
             Files.write(keyFile, new ObjectMapper().writeValueAsString(storedWallet).getBytes());
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         PassphraseProtectedWallet passphraseProtectedWallet = new PassphraseProtectedWallet(wallet);
         passphraseProtectedWallet.setPassword(pass);
         return passphraseProtectedWallet;
     }

     @Override
     public PassphraseProtectedWallet createEthereum(String pass) {
         Wallet wallet = generateEthWallet();
         LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeSource.getTime()), ZoneId.systemDefault());
         String keyPath = String.format(FORMAT, version, FORMATTER.format(currentTime), wallet.getAppAccount());
         Path keyFile = keystoreDirPath.resolve(keyPath);
         if (Files.exists(keyFile)) {
             throw new RuntimeException("key file already exits. Should not happen");
         }
         if (StringUtils.isBlank(pass)) {
             pass = generator.generate();
         }
         byte[] encryptedPrivateKey = CryptoUtils.encryptAes(wallet.getPrivateKey(), sha256().digest(pass.getBytes()));
         StoredWallet storedWallet = new StoredWallet(wallet.getAccount(), Convert.toHexString(wallet.getPublicKey()), Convert.toHexString(encryptedPrivateKey), currentTime);
         try {
             Files.write(keyFile, new ObjectMapper().writeValueAsString(storedWallet).getBytes());
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         PassphraseProtectedWallet protectedWallet = new PassphraseProtectedWallet(wallet);
         protectedWallet.setPassword(pass);
         return protectedWallet;
     }

     private Wallet generateBitcoinWallet() {
         try {
             KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
             ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
             g.initialize(spec);
             KeyPair keyPair = g.generateKeyPair();
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
         } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
             throw new RuntimeException(e);
         }
     }

     private Wallet generateEthWallet() {
         try {
             KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
             ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
             g.initialize(spec);
             KeyPair keyPair = g.generateKeyPair();
             MessageDigest sha256 = keccak256();
             byte[] publicKeyHash = sha256.digest(keyPair.getPublic().getEncoded());
             byte[] address = new byte[20];
             System.arraycopy(publicKeyHash, 12, address, 0, 20);
             return new Wallet("0x" + Convert.toHexString(address), keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
         } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
             throw new RuntimeException(e);
         }
     }
 }