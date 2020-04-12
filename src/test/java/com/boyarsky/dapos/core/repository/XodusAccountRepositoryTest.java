package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.StoreExtension;
import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.Wallet;
import com.boyarsky.dapos.utils.CryptoUtils;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XodusAccountRepositoryTest {
    private XodusAccountRepository repository;
    private TransactionManager manager;
    private PersistentEntityStore store;
    @RegisterExtension
    static StoreExtension extension = new StoreExtension(false);
    @BeforeEach
    void setup() {
        store = extension.getStore();
        manager = new TransactionManager(store);
        repository = new XodusAccountRepository(store, manager);
    }

    @AfterEach
    void cleanup() {

    }

    @Test
    void saveAndFind() {
        manager.begin();
        Account accToSave = generateAcc();
        repository.save(accToSave);
        Account foundAccount = repository.find(accToSave.getCryptoId());
        assertEquals(accToSave, foundAccount);
        manager.commit();
        Account afterTxAccount = repository.find(accToSave.getCryptoId());
        assertEquals(accToSave, afterTxAccount);
    }

    private Account generateAcc() {
        Random random = new Random();
        Wallet wallet = CryptoUtils.generateEd25Wallet();
        Account acc = new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), random.nextInt(1000), Account.Type.ORDINARY);
        acc.setHeight(random.nextInt(103));
        return acc;
    }

    @Test
    void saveWithoutTx() {
        assertThrows(IllegalStateException.class, () -> repository.save(new Account()));
    }

    @Test
    void getAll() {
        Account acc1 = generateAcc();
        Account acc2 = generateAcc();
        Account acc3 = generateAcc();
        manager.begin();
        repository.save(acc1);
        repository.save(acc2);
        repository.save(acc3);
        manager.commit();
        List<Account> all = repository.getAll();
        assertEquals(List.of(acc1, acc2, acc3), all);
    }

    @Test
    void delete() {
        assertThrows(UnsupportedOperationException.class, () -> repository.delete(generateAcc()));
    }
}