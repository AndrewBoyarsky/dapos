package com.boyarsky.dapos.core.repository.currency;

import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {RepoTest.Config.class, XodusCurrencyRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.currency")
class XodusCurrencyRepositoryTest extends RepoTest {

    @Autowired
    CurrencyRepository repo;
    @Autowired
    TransactionManager manager;

    AccountId acc1 = CryptoUtils.generateEthWallet().getAccount();
    AccountId acc2 = CryptoUtils.generateBitcoinWallet().getAccount();
    AccountId acc3 = CryptoUtils.generateValidatorWallet().getAccount();
    Currency cur1 = new Currency(1001, 1, "CDD", "Cooper dump dub", "Unit test repo CDD coin", acc1, 9000, 322, (byte) 2);
    Currency cur2 = new Currency(1002, 2, "GMC", "General motors coin", "Unit test repo GMC coin", acc2, 32000, 111111, (byte) 2);
    Currency cur3 = new Currency(1002, 3, "RERTR", "Rertr coin", "Unit test repo RERTR coin", acc3, 43093, 10000, (byte) 5);
    Currency cur4 = new Currency(1003, 4, "SUPER", "Superman coin", "Unit test repo superman coin", acc1, 4333, 9000, (byte) 5);

    @BeforeEach
    void setUp() {
        manager.begin();
        repo.save(cur1);
        repo.save(cur2);
        repo.save(cur3);
        repo.save(cur4);
        manager.commit();
    }

    @Test
    void update() {
        cur1.setSupply(0);
        manager.begin();

        repo.save(cur1);
        manager.commit();

        Currency currency = repo.get(cur1.getCurrencyId());
        assertEquals(cur1, currency);
    }

    @Test
    void get() {
        Currency currency = repo.get(cur4.getCurrencyId());
        assertEquals(cur4, currency);
    }

    @Test
    void getAll() {
        List<Currency> all = repo.getAll();

        assertEquals(List.of(cur4, cur3, cur2, cur1), all);
    }

    @Test
    void getByCode() {
        Currency byCode = repo.getByCode(cur2.getCode());
        assertEquals(cur2, byCode);
    }
}