package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionHandlerTest {
    AccountId senderId = new AccountId("2500000000000000000000000000000000");
    AccountId recipientId = new AccountId("2500000000000000000000000000000001");

    private DefaultTransactionHandler handler;
    @Mock
    private AccountService service;

    @BeforeEach
    void setUp() {
        handler = new DefaultTransactionHandler(service);
    }

    @Test
    void handle_burnMoney_noTransferZeroAmount() {
        Transaction tx = new Transaction((byte) 1, TxType.CHANGE_FEE_PROVIDER, senderId, new byte[32], recipientId, new byte[0], 0, 1, null);

        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, null, 1);
        verifyNoMoreInteractions(service);
    }

    @Test
    void handle_noTransferNoRecipient() {
        Transaction tx = new Transaction((byte) 1, TxType.CHANGE_FEE_PROVIDER, senderId, new byte[32], null, new byte[0], 1, 0, null);

        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, null, 0);
        verifyNoMoreInteractions(service);
    }

    @Test
    void handle_withTransfer() {
        Transaction tx = new Transaction((byte) 1, TxType.CHANGE_FEE_PROVIDER, senderId, new byte[32], recipientId, new byte[0], 1, 2, null);

        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, recipientId, 1);
        verify(service).transferMoney(senderId, null, 2);
        verifyNoMoreInteractions(service);
    }
}