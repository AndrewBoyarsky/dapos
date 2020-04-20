package com.boyarsky.dapos.core.repository.feeprov;

import com.boyarsky.dapos.AccountUtil;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {RepoTest.Config.class, XodusAccountFeeRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.feeprov")
class XodusAccountFeeRepositoryTest extends RepoTest {
    @Autowired
    AccountFeeRepository repository;
    private AccountId alice = AccountUtil.generateEd25Acc().getCryptoId();
    AccountFeeAllowance allowance1 = new AccountFeeAllowance(alice, 1, 10, 20);
    AccountFeeAllowance allowance4 = new AccountFeeAllowance(alice, 3, 0, 0);
    private AccountId bob = AccountUtil.generateEd25Acc().getCryptoId();
    AccountFeeAllowance allowance2 = new AccountFeeAllowance(bob, 1, 2, 3);
    AccountFeeAllowance allowance3 = new AccountFeeAllowance(bob, 2, 1, 5);

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
        AccountFeeAllowance allowance = repository.getBy(allowance2.getProvId(), allowance2.getAccount());
        assertEquals(allowance2, allowance);
    }

    @Test
    void save() {
        manager.begin();

        repository.save(allowance4);
        manager.commit();

        assertEquals(allowance4, repository.getBy(allowance4.getProvId(), allowance4.getAccount()));
    }
}