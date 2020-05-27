package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.service.message.MessageService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.handler.impl.DefaultTransactionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void testChargeFeeProvider() throws IOException {
        Transaction tx = new Transaction((byte) 1, TxType.PAYMENT, senderId, new byte[32], null, new byte[0], 893, 2, 2000, new byte[64]);
        tx.setHeight(100);
        tx.setGasUsed(111);
        tx.putAttachment(new PaymentAttachment());
        tx.putAttachment(new NoFeeAttachment((byte) 1, 322));

        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(feeProviderService).charge(322, 222, 100, senderId, null, tx.getTxId());
        verifyNoInteractions(messageService);
    }

    @Test
    void testAddMessage() {
        Transaction tx = new Transaction((byte) 1, TxType.PAYMENT, senderId, new byte[32], null, new byte[0], 232, 5, 2000, new byte[64]);
        tx.putAttachment(new PaymentAttachment());
        tx.putAttachment(new MessageAttachment((byte) 1, new EncryptedData(new byte[32], new byte[32]), true, true));
        tx.setHeight(140);
        tx.setGasUsed(14);

        handler.handle(tx);

        verify(service).assignPublicKey(senderId, new byte[32]);
        verify(service).transferMoney(senderId, null, new Operation(tx.getTxId(), 140, "Tx Fee", 70));
        verifyNoInteractions(feeProviderService);
    }
}