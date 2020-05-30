package com.boyarsky.dapos.core.repository.ledger;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ComponentScan("com.boyarsky.dapos.core.repository.ledger")
@ContextConfiguration(classes = {RepoTest.Config.class, XodusLedgerRepositoryTest.class})
class XodusLedgerRepositoryTest extends RepoTest {
    @Autowired
    LedgerRepository repo;
    AccountId id1 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId id2 = TestUtil.generateEd25Acc().getCryptoId();
    AccountId id3 = TestUtil.generateEd25Acc().getCryptoId();
    //    AccountId id4 = TestUtil.generateEd25Acc().getCryptoId();
    LedgerRecord rec1 = new LedgerRecord(1, -100, "PAYMENT", id1, id2, 90);
    LedgerRecord rec2 = new LedgerRecord(2, -90, "Tx fee", id1, null, 9);
    LedgerRecord rec3 = new LedgerRecord(100, 300, "Genesis init", null, id3, 100);
    LedgerRecord rec4 = new LedgerRecord(-434, -333, "Charge fee provider", null, null, 100);
    LedgerRecord rec5 = new LedgerRecord(3, -328, "PAYMENT", id2, id1, 101);
    LedgerRecord rec6 = new LedgerRecord(4, 20, "PAYMENT", id2, id3, 102);

    @Autowired
    TransactionManager manager;

    @BeforeEach
    void setUp() {
        manager.begin();
        repo.save(rec1);
        repo.save(rec2);
        repo.save(rec3);
        repo.save(rec4);
        repo.save(rec5);
        repo.save(rec6);
        manager.commit();
    }

    @Test
    void getRecords() {
        List<LedgerRecord> records = repo.getRecords(id1, null);

        assertEquals(List.of(rec5, rec1, rec2), records);
    }

    @Test
    void getRecords_withZeroPagePagination() {
        List<LedgerRecord> records = repo.getRecords(id1, new Pagination(0, 2));

        assertEquals(List.of(rec5, rec1), records);
    }

    @Test
    void getRecords_withSecondPagePagination() {
        List<LedgerRecord> records = repo.getRecords(id1, new Pagination(2, 1));

        assertEquals(List.of(rec2), records);
    }

    @Test
    void getRecords_byType() {
        List<LedgerRecord> payments = repo.getRecords(id2, "PAYMENT", null);

        assertEquals(List.of(rec6, rec5, rec1), payments);

        payments = repo.getRecords(id2, "PAYMENT", new Pagination(2, 2));

        assertEquals(List.of(), payments);

        payments = repo.getRecords(id2, "PAYMENT", new Pagination(1, 2));

        assertEquals(List.of(rec1), payments);
    }
}