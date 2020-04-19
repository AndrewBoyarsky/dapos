package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.repository.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.boyarsky.dapos.AccountUtil.generateAcc;
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
        Account accToSave = generateAcc();
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
        manager.begin();
        assertThrows(UnsupportedOperationException.class, () -> repository.delete(generateAcc()));
        manager.rollback();
    }
}