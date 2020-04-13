package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.utils.Convert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GenesisTest {
    ObjectMapper mapper = new ObjectMapper();
    @Mock
    AccountService accountService;
    private Account account1 = new Account(new AccountId("2523d033afe32513eb09474d64fb0a82f3"), Convert.parseHexString("14642f203d0984772b28beb7db565213f512c58bcb9897def473c5401b1b69bb"), 1000, Account.Type.ORDINARY);
    private Account account2 = new Account(new AccountId("25b81ffe5e76d6eb361d6a5dc37c766dfb"), Convert.parseHexString("23891f123d1ecf2216070ca02e6a783ddd1e5c75dfb2f54a93fc5b5bbd24dfe0"), 200, Account.Type.ORDINARY);
    private Account account3 = new Account(new AccountId("1P8LnS8QAVe23GGfdoy9XBU9hGacDaS1xe"), Convert.parseHexString("02a82572fb4c702eacdf52277c55017fe3639f5e0fe15bedd7baa21e7b5b7f7c1d"), 100, Account.Type.ORDINARY);
    private Account account4 = new Account(new AccountId("0xd3ef7139bdea050bd26543294aad956c1333a723"), Convert.parseHexString("02693b0376895254c0ffbb778accfeb6730d8ac169ee9cad799d971d43a0450a87"), 900, Account.Type.ORDINARY);

    @Test
    void initialize() {
        GenesisImpl genesis = new GenesisImpl(accountService, mapper);
        int initialize = genesis.initialize();
        assertEquals(4, initialize);
        verify(accountService).save(account1);
        verify(accountService).save(account2);
        verify(accountService).save(account3);
        verify(accountService).save(account4);
    }

    @Test
    void initialize_wrong_balance_format() {
        assertThrows(RuntimeException.class, () -> new GenesisImpl(accountService, mapper, "incorrect-genesis-accounts.json").initialize());
    }
}