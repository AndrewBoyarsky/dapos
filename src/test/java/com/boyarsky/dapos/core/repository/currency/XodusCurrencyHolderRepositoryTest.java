package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {RepoTest.Config.class, XodusCurrencyHolderRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.currency")
class XodusCurrencyHolderRepositoryTest extends RepoTest {
    @Autowired
    CurrencyHolderRepository repository;
    @Autowired
    TransactionManager manager;

    AccountId acc1 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId acc2 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId acc3 = TestUtil.generateEd25Acc().getCryptoId();
    CurrencyHolder holder1 = new CurrencyHolder(300, acc1, 2, 454);
    CurrencyHolder holder2 = new CurrencyHolder(301, acc2, 2, 654);
    CurrencyHolder holder3 = new CurrencyHolder(299, acc3, 2, 788);
    CurrencyHolder holder4 = new CurrencyHolder(400, acc1, 3, 344);
    CurrencyHolder holder5 = new CurrencyHolder(300, acc2, 3, 866);

    @BeforeEach
    void setUp() {
        manager.begin();
        repository.save(holder1);
        repository.save(holder2);
        repository.save(holder3);
        repository.save(holder4);
        repository.save(holder5);
        manager.commit();
    }

    @Test
    void update() {
        holder3.setAmount(999);
        holder3.setHeight(900000);
        manager.begin();
        repository.save(holder3);
        manager.commit();
        CurrencyHolder holder = repository.get(holder3.getHolder(), holder3.getCurrencyId());
        assertEquals(holder3, holder);
    }

    @Test
    void get() {
        CurrencyHolder holder = repository.get(holder2.getHolder(), holder2.getCurrencyId());

        assertEquals(holder2, holder);
    }

    @Test
    void getAllForCurrency() {
        List<CurrencyHolder> allHolders = repository.getAllForCurrency(holder2.getCurrencyId(), pagination);

        assertEquals(List.of(holder2, holder1, holder3), allHolders);
    }

    @Test
    void getAllByAccount() {
        List<CurrencyHolder> allByAccount = repository.getAllByAccount(acc2, new Pagination());

        assertEquals(List.of(holder2, holder5), allByAccount);
    }
}