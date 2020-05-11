package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.repository.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.boyarsky.dapos.TestUtil.generateEd25Acc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ComponentScan("com.boyarsky.dapos.core.repository.account")
@ContextConfiguration(classes = {RepoTest.Config.class, XodusAccountRepositoryTest.class})
public class XodusAccountRepositoryTest extends RepoTest {
    @Autowired
    AccountRepository repository;

    @Test
    void saveAndFind() {
        manager.begin();
        Account accToSave = generateEd25Acc();
        repository.save(accToSave);
        Account foundAccount = repository.find(accToSave.getCryptoId());
        assertEquals(accToSave, foundAccount);
        manager.commit();
        Account afterTxAccount = repository.find(accToSave.getCryptoId());
        assertEquals(accToSave, afterTxAccount);
    }


    @Test
    void saveWithoutTx() {
        assertThrows(IllegalStateException.class, () -> repository.save(new Account()));
    }

    @Test
    void getAll() {
        Account acc1 = generateEd25Acc();
        Account acc2 = generateEd25Acc();
        Account acc3 = generateEd25Acc();
        manager.begin();
        repository.save(acc1);
        repository.save(acc2);
        repository.save(acc3);
        manager.commit();
        manager.begin();
        List<Account> all = repository.getAll();
        assertEquals(List.of(acc1, acc2, acc3), all);
        manager.commit();
    }

    @Test
    void delete() {
        manager.begin();
        assertThrows(UnsupportedOperationException.class, () -> repository.delete(generateEd25Acc()));
        manager.rollback();
    }
}