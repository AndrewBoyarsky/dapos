package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.utils.Convert;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class GenesisTest {
    ObjectMapper mapper = new ObjectMapper();
    @Mock
    AccountService accountService;
    private final byte[] pubKey1 = Convert.parseHexString("8bd574fdb05c2dc5017188a2f4c32d5b81963e0a33eccba92404e968c665006d");
    private Account account1 = new Account(new AccountId("2523d033afe32513eb09474d64fb0a82f3"), Convert.parseHexString("14642f203d0984772b28beb7db565213f512c58bcb9897def473c5401b1b69bb"), 1000, Account.Type.ORDINARY);
    private Account account2 = new Account(new AccountId("25b81ffe5e76d6eb361d6a5dc37c766dfb"), Convert.parseHexString("23891f123d1ecf2216070ca02e6a783ddd1e5c75dfb2f54a93fc5b5bbd24dfe0"), 200, Account.Type.ORDINARY);
    private Account account3 = new Account(new AccountId("1P8LnS8QAVe23GGfdoy9XBU9hGacDaS1xe"), Convert.parseHexString("02a82572fb4c702eacdf52277c55017fe3639f5e0fe15bedd7baa21e7b5b7f7c1d"), 100, Account.Type.ORDINARY);
    private Account account4 = new Account(new AccountId("0xd3ef7139bdea050bd26543294aad956c1333a723"), Convert.parseHexString("02693b0376895254c0ffbb778accfeb6730d8ac169ee9cad799d971d43a0450a87"), 900, Account.Type.ORDINARY);
    private final byte[] pubKey2 = Convert.parseHexString("427e170f76f81f7742e9da100d71346aa631acb3f9980a1a41680883c0654431");
    @Mock
    ValidatorService validatorService;
    private ValidatorEntity validator1 = new ValidatorEntity(true, pubKey1, 0, new AccountId(CryptoUtils.validatorAddress(pubKey1)), new AccountId("1P8LnS8QAVe23GGfdoy9XBU9hGacDaS1xe"), 0, 100, 3500);
    private ValidatorEntity validator2 = new ValidatorEntity(true, pubKey2, 0, new AccountId(CryptoUtils.validatorAddress(pubKey2)), new AccountId("0xd3ef7139bdea050bd26543294aad956c1333a723"), 0, 200, 10);

    @Test
    void initialize() {
        GenesisImpl genesis = new GenesisImpl(accountService, validatorService, mapper);
        doReturn(validator1).when(validatorService).registerValidator(pubKey1, validator1.getFee(), validator1.getRewardId(), true, 0);
        doReturn(validator2).when(validatorService).registerValidator(pubKey2, validator2.getFee(), validator2.getRewardId(), true, 0);
        GenesisInitResult initResponse = genesis.initialize();
        assertEquals(4, initResponse.getNumberOfAccount());
        assertEquals(List.of(validator1, validator2), initResponse.getValidatorEntities());
        for (Account account : List.of(account1, account2, account3, account4)) {
            verify(accountService).addToBalance(account.getCryptoId(), null, new Operation(0, 0, "GENESIS_BALANCE", account.getBalance()));
            verify(accountService).assignPublicKey(account.getCryptoId(), account.getPublicKey());
        }
        verify(accountService).addToBalance(validator1.getRewardId(), null, new Operation(0, 0, "Init Validator Balance", 100));
        verify(accountService).addToBalance(validator2.getRewardId(), null, new Operation(0, 0, "Init Validator Balance", 200));
        verify(validatorService).addVote(validator1.getId(), validator1.getRewardId(), 100, 0);
        verify(validatorService).addVote(validator2.getId(), validator2.getRewardId(), 200, 0);
    }


    @Test
    void initialize_wrong_balance_format() {
        assertThrows(RuntimeException.class, () -> new GenesisImpl(accountService, validatorService, mapper, "incorrect-genesis-accounts.json").initialize());
    }

    @Test
    void initialize_fee_scale_is_too_big() {
        GenesisImpl genesis = new GenesisImpl(accountService, validatorService, mapper, "incorrect-validators-genesis-fee-big-scale.json");

        assertThrows(IllegalArgumentException.class, genesis::initialize);
    }


    @Test
    void initialize_fee_is_negative() {
        GenesisImpl genesis = new GenesisImpl(accountService, validatorService, mapper, "incorrect-validators-genesis-fee-negative.json");

        assertThrows(IllegalArgumentException.class, genesis::initialize);
    }

    @Test
    void initialize_fee_is_too_big() {
        GenesisImpl genesis = new GenesisImpl(accountService, validatorService, mapper, "incorrect-validators-genesis-fee-too-big.json");

        assertThrows(IllegalArgumentException.class, genesis::initialize);
    }
}