package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.core.TimeSource;
import com.boyarsky.dapos.core.TimeSourceImpl;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.utils.CryptoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

import static com.boyarsky.dapos.utils.CryptoUtils.sha256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class KeyStoreServiceTest {
    @Mock
    PassphraseGenerator passphraseGenerator;
    @Mock
    TimeSource timeSource;
    String bitcoinWalletFile = "v1_2020-03-30_22-58-35---dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALaa4";

    @Test
    void testCreateBitcoin(@TempDir Path dir) throws IOException, InvalidKeySpecException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        doReturn("12345").when(passphraseGenerator).generate();
        PassphraseProtectedWallet bitcoin = keystore.createBitcoin(null);

        assertTrue(bitcoin.getAccount().isBitcoin());
        List<Path> files = Files.walk(dir).filter(e -> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("12345".getBytes()));
        assertEquals(new PassphraseProtectedWallet(wallet.getAccount().substring(3), CryptoUtils.getKeyPair(wallet.getCryptoAlgo(), privateKey, Convert.parseHexString(wallet.getPublicKey())), "12345"), bitcoin);

    }


    //    @Test
    void testGenerateAccounts(@TempDir Path dir) {
        KeyStoreService service = new KeyStoreService(dir, new TimeSourceImpl(), passphraseGenerator);
        Wallet acc1 = service.createEd25("12345");
        Wallet acc2 = service.createEd25("12345");
        Wallet acc3 = service.createBitcoin("12345");
        Wallet acc4 = service.createEthereum("12345");
        List<Account> accounts = List.of(new Account(acc1.getAccount(), CryptoUtils.compress(acc1.getKeyPair().getPublic()), 1000, Account.Type.ORDINARY),
                new Account(acc2.getAccount(), CryptoUtils.compress(acc2.getKeyPair().getPublic()), 1000, Account.Type.ORDINARY),
                new Account(acc3.getAccount(), CryptoUtils.compress(acc3.getKeyPair().getPublic()), 1000, Account.Type.ORDINARY),
                new Account(acc4.getAccount(), CryptoUtils.compress(acc4.getKeyPair().getPublic()), 1000, Account.Type.ORDINARY)
        );
        for (Account account : accounts) {
            System.out.println(account);
        }
    }

    @Test
    void testCreateEd25(@TempDir Path dir) throws IOException, InvalidKeySpecException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        String mypass = "i'll be back";

        PassphraseProtectedWallet ed25 = (PassphraseProtectedWallet) keystore.createEd25(mypass);

        assertTrue(ed25.getAccount().isEd25());
        List<Path> files = Files.walk(dir).filter(e -> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest(mypass.getBytes()));
        assertEquals(new PassphraseProtectedWallet(wallet.getAccount().substring(3), CryptoUtils.getKeyPair(wallet.getCryptoAlgo(), privateKey, Convert.parseHexString(wallet.getPublicKey())), mypass), ed25);
        assertEquals("Ed25519",ed25.getKeyPair().getPublic().getAlgorithm());
    }

    @Test
    void testGetWallet() throws URISyntaxException {
        KeyStoreService keystore = new KeyStoreService(Paths.get(getClass().getClassLoader().getResource(bitcoinWalletFile).toURI()).getParent(), timeSource, passphraseGenerator);
        VerifiedWallet verified = keystore.getWallet("dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALaa4", "12345");
        assertEquals(Status.OK, verified.getExtractStatus());
        assertEquals("EC", verified.getWallet().getKeyPair().getPrivate().getAlgorithm());
        assertEquals("dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALaa4", verified.getWallet().getAppSpecificAccount());
    }


    @Test
    void testGetWallet_incorrectPass() throws URISyntaxException {
        KeyStoreService keystore = new KeyStoreService(Paths.get(getClass().getClassLoader().getResource(bitcoinWalletFile).toURI()).getParent(), timeSource, passphraseGenerator);
        VerifiedWallet verified = keystore.getWallet("dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALaa4", "1234");
        assertEquals(Status.BAD_CREDENTIALS, verified.getExtractStatus());
    }

    @Test
    void testGetWallet_notFound() throws URISyntaxException {
        KeyStoreService keystore = new KeyStoreService(Paths.get(getClass().getClassLoader().getResource(bitcoinWalletFile).toURI()).getParent(), timeSource, passphraseGenerator);
        VerifiedWallet verified = keystore.getWallet("dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALab4", "1234");
        assertEquals(Status.NOT_FOUND, verified.getExtractStatus());
    }

    @Test
    void testGetWallet_duplicate() throws URISyntaxException {
        KeyStoreService keystore = new KeyStoreService(Paths.get(getClass().getClassLoader().getResource(bitcoinWalletFile).toURI()).getParent(), timeSource, passphraseGenerator);
        VerifiedWallet verified = keystore.getWallet("det0x3c4f172d52e40097305539df2d23b3c5d20f05a2", "passphrase");
        assertEquals(Status.DUPLICATE_FOUND, verified.getExtractStatus());
    }

    @Test
    void createEthereum(@TempDir Path dir) throws IOException, InvalidKeySpecException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        PassphraseProtectedWallet bitcoin = keystore.createEthereum("passphrase");

        assertTrue(bitcoin.getAccount().isEth());
        List<Path> files = Files.walk(dir).filter(e -> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("passphrase".getBytes()));
        assertEquals(bitcoin, new PassphraseProtectedWallet(wallet.getAccount().substring(3), CryptoUtils.getKeyPair(wallet.getCryptoAlgo(), privateKey, Convert.parseHexString(wallet.getPublicKey())), "passphrase"));
    }
}