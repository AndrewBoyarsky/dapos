package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.stereotype.Component;

@Component
public class MultiPaymentTxHandler implements TransactionTypeHandler {
    private AccountService accountService;

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        MultiAccountAttachment attachment = tx.getAttachment(MultiAccountAttachment.class);
        accountService.multiTransferMoney(tx.getSender(), attachment.getTransfers(), new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), 0));
    }

    @Override
    public TxType type() {
        return TxType.MULTI_PAYMENT;
    }
}
