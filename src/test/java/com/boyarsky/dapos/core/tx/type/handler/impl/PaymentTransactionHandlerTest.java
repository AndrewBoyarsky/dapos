package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionHandlerTest {
    AccountId senderId = new AccountId("2500000000000000000000000000000011");
    AccountId recipientId = new AccountId("2500000000000000000000000000000022");

    @Mock
    AccountService service;
    PaymentTransactionHandler handler;


    @BeforeEach
    void setUp() {
        handler = new PaymentTransactionHandler(service);
    }

    @Test
    void handle_burnMoneyNoRecipient() {
        Transaction tx = new Transaction((byte) 1, TxType.PAYMENT, senderId, new byte[32], null, new byte[0], 1, 2, 5, null);
        tx.setHeight(20);

        handler.handle(tx);

        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 20, "PAYMENT", 1));
    }

    @Test
    void handle_withTransfer() {
        Transaction tx = new Transaction((byte) 1, TxType.PAYMENT, senderId, new byte[32], recipientId, new byte[0], 1, 2, 100, null);
        tx.setHeight(30);
        handler.handle(tx);

        verify(service).transferMoney(senderId, recipientId, new Operation(tx.getTxId(), 30, "PAYMENT", 1));
    }
}