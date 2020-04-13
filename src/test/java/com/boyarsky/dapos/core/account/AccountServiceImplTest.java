package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.core.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    AccountId id1 = new AccountId("2500000000000000000000000000000000");
    AccountId id2 = new AccountId("2500000000000000000000000000000001");
    Account expected1 = new Account(id1, new byte[32], 100, Account.Type.ORDINARY);
    Account expected2 = new Account(id2, new byte[32], 101, Account.Type.ORDINARY);
    @Mock
    AccountRepository repository;
    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(repository);
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

        service.transferMoney(id1, id2, 90);

        verify(repository).save(new Account(id2, null, 90, Account.Type.ORDINARY));
        verify(repository).save(expected1);
        assertEquals(10, expected1.getBalance());
    }

    @Test
    void transferMoney_recipient_exist() {
        doReturn(expected1).when(repository).find(id1);
        doReturn(expected2).when(repository).find(id2);

        service.transferMoney(id1, id2, 50);

        verify(repository).save(expected2);
        verify(repository).save(expected1);
        assertEquals(50, expected1.getBalance());
        assertEquals(151, expected2.getBalance());
    }


    @Test
    void save() {
        service.save(expected2);

        verify(repository).save(expected2);
    }
}