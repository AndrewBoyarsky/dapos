package com.boyarsky.dapos.account;

import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.utils.CryptoUtils;
import com.boyarsky.dapos.core.TimeSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import java.security.Security;
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
    void testCreateBitcoin(@TempDir Path dir) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        doReturn("12345").when(passphraseGenerator).generate();
        PassphraseProtectedWallet bitcoin = keystore.createBitcoin(null);

        assertTrue(bitcoin.getAccount().isBitcoin());
        List<Path> files = Files.walk(dir).filter(e-> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("12345".getBytes()));
        assertEquals(new PassphraseProtectedWallet(wallet.getAccount().substring(3), Convert.parseHexString(wallet.getPublicKey()), privateKey, "12345"), bitcoin);

    }

    @Test
    void testGetWallet() throws URISyntaxException {
        KeyStoreService keystore = new KeyStoreService(Paths.get(getClass().getClassLoader().getResource(bitcoinWalletFile).toURI()).getParent(), timeSource, passphraseGenerator);
        VerifiedWallet verified = keystore.getWallet("dab19SGWN7jeX7S6P3ay4hEyH6qDfkDzALaa4", "12345");
        assertEquals(Status.OK, verified.getExtractStatus());
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
    void createEthereum(@TempDir Path dir) throws IOException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        PassphraseProtectedWallet bitcoin = keystore.createEthereum("passphrase");

        assertTrue(bitcoin.getAccount().isEth());
        List<Path> files = Files.walk(dir).filter(e-> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("passphrase".getBytes()));
        assertEquals(bitcoin, new PassphraseProtectedWallet(wallet.getAccount().substring(3), Convert.parseHexString(wallet.getPublicKey()), privateKey, "12345"));
    }
}