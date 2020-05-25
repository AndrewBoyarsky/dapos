package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ContextConfiguration(classes = {RepoTest.Config.class, XodusValidatorRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.validator")
public class XodusValidatorRepositoryTest extends RepoTest {

    @Autowired
    ValidatorRepository repository;
    Account account1 = TestUtil.generateValidatorAcc();
    Account account2 = TestUtil.generateValidatorAcc();
    Account account3 = TestUtil.generateValidatorAcc();
    Account account4 = TestUtil.generateValidatorAcc();
    AccountId acc = TestUtil.generateEd25Acc().getCryptoId();

    ValidatorEntity v1 = new ValidatorEntity(true, account1.getPublicKey(), 43, account1.getCryptoId(), acc, 10, 123, 500);
    ValidatorEntity v2 = new ValidatorEntity(false, account2.getPublicKey(), 1, account2.getCryptoId(), acc, 121, 111, 0);
    ValidatorEntity v3 = new ValidatorEntity(true, account3.getPublicKey(), 32, account3.getCryptoId(), acc, 90, 290, 10000);
    ValidatorEntity v4 = new ValidatorEntity(true, account4.getPublicKey(), 0, account4.getCryptoId(), acc, 0, 332, 1213);

    @BeforeEach
    void setUp() {
        v1.setHeight(10);
        v2.setHeight(10);
        manager.begin();
        repository.save(v1);
        repository.save(v2);
        repository.save(v3);
        manager.commit();
    }

    @Test
    void save() {
        manager.begin();
        repository.save(v4);
        ValidatorEntity entity = repository.getById(v4.getId());
        manager.commit();
        assertEquals(entity, v4);
        assertEquals(4, repository.getAll().size());
    }

    @Test
    void testUpdate() {
        v1.setAbsentFor(Integer.MAX_VALUE);
        manager.begin();
        repository.save(v1);
        manager.commit();
        assertEquals(3, repository.getAll().size());
        assertEquals(v1, repository.getById(v1.getId()));
    }

    @Test
    void testGetAll() {
        List<ValidatorEntity> entities = repository.getAll();

        assertEquals(List.of(v3, v1, v2), entities);
    }

    @Test
    void getById() {
        ValidatorEntity byId = repository.getById(v2.getId());

        assertEquals(v2, byId);
    }

    @Test
    void testGetByHeight() {
        List<ValidatorEntity> all = repository.getAll(10);

        assertEquals(List.of(v1, v2), all);
    }
}