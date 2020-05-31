package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.account.AccountRepository;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.AccountServiceImpl;
import com.boyarsky.dapos.core.service.account.NotFoundException;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    AccountId id1 = new AccountId("2500000000000000000000000000000000");
    AccountId id2 = new AccountId("2500000000000000000000000000000002");
    AccountId id3 = new AccountId("2500000000000000000000000000000003");
    AccountId id4 = new AccountId("2500000000000000000000000000000004");
    AccountId id5 = new AccountId("2500000000000000000000000000000005");
    Account expected1 = new Account(id1, new byte[32], 100, Account.Type.ORDINARY);
    Account expected2 = new Account(id2, new byte[32], 101, Account.Type.ORDINARY);
    Account expected3 = new Account(id3, new byte[32], 200, Account.Type.ORDINARY);
    Account expected4 = new Account(id4, new byte[32], 300, Account.Type.ORDINARY);
    @Mock
    AccountRepository repository;
    @Mock
    LedgerService ledgerService;
    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(repository, ledgerService);
    }

    @Test
    void get() {
        doReturn(expected1).when(repository).find(id1);

        Account res = service.get(id1);

        assertEquals(expected1, res);
    }

    @Test
    void get_not_found() {
        assertThrows(NotFoundException.class, () -> service.get(id1));
    }

    @Test
    void getAll() {
        List<Account> expectedList = List.of(this.expected1, expected2);
        doReturn(expectedList).when(repository).getAll();

        List<Account> all = service.getAll();

        assertEquals(expectedList, all);
    }

    @Test
    void transferMoney_recipient_not_exist() {
        doReturn(expected1).when(repository).find(id1);
        doReturn(null).when(repository).find(id2);

        service.transferMoney(id1, id2, new Operation(0, 0, "Transfer", 90));

        verify(repository).save(new Account(id2, null, 90, Account.Type.ORDINARY));
        verify(repository).save(expected1);
        assertEquals(10, expected1.getBalance());
    }

    @Test
    void transferMoney_recipient_exist() {
        doReturn(expected1).when(repository).find(id1);
        doReturn(expected2).when(repository).find(id2);

        service.transferMoney(id1, id2, new Operation(0, 0, "Transfer", 50));

        verify(repository).save(expected2);
        verify(repository).save(expected1);
        assertEquals(50, expected1.getBalance());
        assertEquals(151, expected2.getBalance());
    }

    @Test
    void multiTransferMoney() {
        doReturn(expected1).when(repository).find(id1);
        doReturn(expected2).when(repository).find(id2);
        doReturn(expected3).when(repository).find(id3);
        doReturn(expected4).when(repository).find(id4);
        doReturn(null).when(repository).find(id5);


        service.multiTransferMoney(id1, Map.of(
                id2, 10L,
                id3, 15L,
                id4, 25L,
                id5, 10L
        ), new Operation(-1, 1, "MULTI_TRANSFER", 0));

        verify(repository).save(expected2);
        verify(repository).save(expected3);
        verify(repository).save(expected4);
        Account account = new Account(id5, null, 10, Account.Type.ORDINARY);
        account.setHeight(1);
        verify(repository).save(account);
        verify(repository).save(expected1);
        verify(ledgerService).add(new LedgerRecord(-1, 10, "MULTI_TRANSFER", id1, id2, 1));
        verify(ledgerService).add(new LedgerRecord(-1, 15, "MULTI_TRANSFER", id1, id3, 1));
        verify(ledgerService).add(new LedgerRecord(-1, 25, "MULTI_TRANSFER", id1, id4, 1));
        verify(ledgerService).add(new LedgerRecord(-1, 10, "MULTI_TRANSFER", id1, id5, 1));
        verifyNoMoreInteractions(ledgerService);
        assertEquals(40, expected1.getBalance());
        assertEquals(111, expected2.getBalance());
        assertEquals(215, expected3.getBalance());
        assertEquals(325, expected4.getBalance());
    }

    @Test
    void burnMoney() {
        doReturn(expected1).when(repository).find(id1);

        service.transferMoney(id1, null, new Operation(0, 0, "Burn", 50));

        verify(repository).save(expected1);
        verifyNoMoreInteractions(repository);
        assertEquals(50, expected1.getBalance());
    }

    @Test
    void assignPublicKey_alreadyAssigned() {
        doReturn(expected1).when(repository).find(id1);

        boolean assigned = service.assignPublicKey(id1, expected1.getPublicKey());

        verifyNoMoreInteractions(repository);
        assertFalse(assigned);
    }


    @Test
    void assignPublicKey() {
        doReturn(expected1).when(repository).find(id1);
        expected1.setPublicKey(null);

        boolean assigned = service.assignPublicKey(id1, new byte[64]);

        verify(repository).save(expected1);
        assertEquals(64, expected1.getPublicKey().length);
        assertTrue(assigned);
    }


    @Test
    void save() {
        service.save(expected2);

        verify(repository).save(expected2);
    }
}