package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.service.message.MessageService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.impl.DefaultTransactionHandler;
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
    @Mock
    private FeeProviderService feeProviderService;
    @Mock
    private MessageService messageService;


    @BeforeEach
    void setUp() {
        handler = new DefaultTransactionHandler(service, feeProviderService, messageService);
    }

    @Test
    void handle_burnMoney_noTransferZeroAmount() {
        Transaction tx = new Transaction((byte) 1, TxType.SET_FEE_PROVIDER, senderId, new byte[32], recipientId, new byte[0], 0, 1, 1, null);
        tx.setGasUsed(1);
        tx.setHeight(100);
        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 100, "Tx Fee", 1));
        verifyNoMoreInteractions(service);
    }

    @Test
    void handle_burnMoneyNoRecipient() {
        Transaction tx = new Transaction((byte) 1, TxType.SET_FEE_PROVIDER, senderId, new byte[32], null, new byte[0], 1, 2, 5, null);
        tx.setGasUsed(5);
        tx.setHeight(20);
        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 20, "SET_FEE_PROVIDER", 1));
        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 20, "Tx Fee", 10));
        verifyNoMoreInteractions(service);
    }

    @Test
    void handle_withTransfer() {
        Transaction tx = new Transaction((byte) 1, TxType.PAYMENT, senderId, new byte[32], recipientId, new byte[0], 1, 2, 100, null);
        tx.setGasUsed(2);
        tx.setHeight(30);
        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, recipientId, new Operation(tx.getTxId(), 30, "PAYMENT", 1));
        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 30, "Tx Fee", 4));
        verifyNoMoreInteractions(service);
    }
}