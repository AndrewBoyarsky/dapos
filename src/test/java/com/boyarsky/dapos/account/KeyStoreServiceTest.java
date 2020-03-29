package com.boyarsky.dapos.account;

import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.utils.CryptoUtils;
import com.boyarsky.dapos.core.TimeSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @Test
    void testCreateBitcoin(@TempDir Path dir) throws IOException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        doReturn("12345").when(passphraseGenerator).generate();
        PassphraseProtectedWallet bitcoin = keystore.createBitcoin(null);

        assertTrue(bitcoin.isBitcoin());
        List<Path> files = Files.walk(dir).filter(e-> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("12345".getBytes()));
        assertEquals(bitcoin, new PassphraseProtectedWallet(wallet.getAccount(), Convert.parseHexString(wallet.getPublicKey()), privateKey, "12345"));
    }

    @Test
    void createEthereum(@TempDir Path dir) throws IOException {
        KeyStoreService keystore = new KeyStoreService(dir, timeSource, passphraseGenerator);
        long time = System.currentTimeMillis();
        doReturn(time).when(timeSource).getTime();
        PassphraseProtectedWallet bitcoin = keystore.createEthereum("passphrase");

        assertTrue(bitcoin.isEth());
        List<Path> files = Files.walk(dir).filter(e-> !Files.isDirectory(e)).collect(Collectors.toList());
        assertEquals(1, files.size());
        StoredWallet wallet = new ObjectMapper().readValue(files.get(0).toFile(), StoredWallet.class);
        byte[] privateKey = CryptoUtils.decryptAes(Convert.parseHexString(wallet.getEncryptedPrivateKey()), sha256().digest("passphrase".getBytes()));
        assertEquals(bitcoin, new PassphraseProtectedWallet(wallet.getAccount(), Convert.parseHexString(wallet.getPublicKey()), privateKey, "12345"));
    }
}