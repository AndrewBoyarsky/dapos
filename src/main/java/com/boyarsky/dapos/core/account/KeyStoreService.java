 /*
  * Copyright © 2018 Apollo Foundation
  */

 package com.boyarsky.dapos.core.account;

 import com.apollocurrency.aplwallet.apl.util.StringUtils;
 import com.boyarsky.dapos.core.TimeSource;
 import com.boyarsky.dapos.utils.Convert;
 import com.boyarsky.dapos.utils.CryptoUtils;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;

 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.security.KeyPair;
 import java.security.MessageDigest;
 import java.security.spec.InvalidKeySpecException;
 import java.time.Instant;
 import java.time.LocalDateTime;
 import java.time.ZoneId;
 import java.time.format.DateTimeFormatter;
 import java.util.List;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;

 import static com.boyarsky.dapos.utils.CryptoUtils.generateBitcoinWallet;
 import static com.boyarsky.dapos.utils.CryptoUtils.generateEthWallet;
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
         return save(pass, wallet);
     }

     @Override
     public VerifiedWallet getWallet(String account, String password) {
         FileSearchResult file = findAppropriateFile(account);
         if (!file.status.isOK()) {
             return new VerifiedWallet(null, file.status);
         }
         try {
             StoredWallet storedWallet = mapper.readValue(file.path.toFile(), StoredWallet.class);
             byte[] key = sha256().digest(password.getBytes());
             byte[] encryptedPrivKey = Convert.parseHexString(storedWallet.getEncryptedPrivateKey());

             String mac = Convert.toHexString(generateMac(encryptedPrivKey, key));
             if (mac.equalsIgnoreCase(storedWallet.getMac())) {
                 byte[] decrypted = CryptoUtils.decryptAes(encryptedPrivKey, key);
                 String crypto = storedWallet.getCryptoAlgo();
                 KeyPair keyPair = new KeyPair(CryptoUtils.getPublicKey(crypto, Convert.parseHexString(storedWallet.getPublicKey())), CryptoUtils.getPrivateKey(crypto, decrypted));
                 return new VerifiedWallet(new Wallet(new AccountId(storedWallet.getAccount()), keyPair), Status.OK);
             } else {
                 return new VerifiedWallet(null, Status.BAD_CREDENTIALS);
             }

         } catch (IOException | InvalidKeySpecException e) {
             throw new RuntimeException(e);
         }
     }


     private FileSearchResult findAppropriateFile(String account) {
         try (Stream<Path> files = Files.walk(keystoreDirPath)) {
             List<Path> allAvailable = files.filter(e -> e.getFileName().toString().endsWith(account) && e.getFileName().toString().startsWith("v" + version)).collect(Collectors.toList());
             if (allAvailable.size() > 1) {
                 return new FileSearchResult(null, Status.DUPLICATE_FOUND);
             }
             if (allAvailable.isEmpty()) {
                 return new FileSearchResult(Status.NOT_FOUND);
             }
             return new FileSearchResult(allAvailable.get(0), Status.OK);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }



     private static class FileSearchResult {
         private Path path;
         private Status status;

         public FileSearchResult(Status status) {
             this.status = status;
         }

         public FileSearchResult(Path path, Status status) {
             this.path = path;
             this.status = status;
         }
     }

     @Override
     public PassphraseProtectedWallet createEthereum(String pass) {
         Wallet wallet = generateEthWallet();
         return save(pass, wallet);
     }

     @Override
     public Wallet createEd25(String pass) {
         Wallet wallet = CryptoUtils.generateEd25Wallet();
         return save(pass, wallet);
     }

     private PassphraseProtectedWallet save(String pass, Wallet wallet) {
         LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeSource.getTime()), ZoneId.systemDefault());
         String keyPath = String.format(FORMAT, version, FORMATTER.format(currentTime), wallet.getAppSpecificAccount());
         Path keyFile = keystoreDirPath.resolve(keyPath);
         if (Files.exists(keyFile)) {
             throw new RuntimeException("key file already exits. Should not happen");
         }
         if (StringUtils.isBlank(pass)) {
             pass = generator.generate();
         }
         byte[] encryptionKey = sha256().digest(pass.getBytes());
         byte[] encryptedPrivateKey = CryptoUtils.encryptAes(wallet.getKeyPair().getPrivate().getEncoded(), encryptionKey);
         byte[] mac = generateMac(encryptedPrivateKey, encryptionKey);
         StoredWallet storedWallet = new StoredWallet(wallet.getAppSpecificAccount(), Convert.toHexString(wallet.getKeyPair().getPublic().getEncoded()), Convert.toHexString(encryptedPrivateKey), wallet.getKeyPair().getPublic().getAlgorithm(), Convert.toHexString(mac), currentTime);
         try {
             Files.write(keyFile, mapper.writeValueAsString(storedWallet).getBytes());
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         PassphraseProtectedWallet protectedWallet = new PassphraseProtectedWallet(wallet);
         protectedWallet.setPassword(pass);
         return protectedWallet;
     }

     public byte[] generateMac(byte[] privKey, byte[] password) {
         MessageDigest digest = sha256();
         digest.update(privKey);
         digest.update(password);
         return digest.digest();
     }
 }