package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ContextConfiguration(classes = {RepoTest.Config.class, XodusAccountFeeRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.feeprov")
class XodusAccountFeeRepositoryTest extends RepoTest {
    @Autowired
    AccountFeeRepository repository;
    private AccountId alice = TestUtil.generateEd25Acc().getCryptoId();
    AccountFeeAllowance allowance1 = new AccountFeeAllowance(alice, 1, false, 10, 20);
    AccountFeeAllowance allowance4 = new AccountFeeAllowance(alice, 1, true, 0, 0);
    private AccountId bob = TestUtil.generateEd25Acc().getCryptoId();
    AccountFeeAllowance allowance2 = new AccountFeeAllowance(bob, 1, true, 2, 3);
    AccountFeeAllowance allowance3 = new AccountFeeAllowance(bob, 2, false, 1, 5);

    @BeforeEach
    void setUp() {
        manager.begin();
        repository.save(allowance1);
        repository.save(allowance2);
        repository.save(allowance3);
        manager.commit();
    }

    @Test
    void getBy() {
        AccountFeeAllowance allowance = repository.getBy(allowance2.getProvId(), allowance2.getAccount(), true);
        assertEquals(allowance2, allowance);

        AccountFeeAllowance recipientAllowance = repository.getBy(allowance2.getProvId(), allowance2.getAccount(), false);
        assertNull(recipientAllowance);
    }

    @Test
    void save() throws InterruptedException {
        manager.begin();
        repository.save(allowance4);
        assertEquals(allowance4, repository.getBy(allowance4.getProvId(), allowance4.getAccount(), true));
        manager.commit();
        AccountFeeAllowance by = repository.getBy(allowance4.getProvId(), allowance4.getAccount(), true);
        assertEquals(allowance4, by);
    }
}