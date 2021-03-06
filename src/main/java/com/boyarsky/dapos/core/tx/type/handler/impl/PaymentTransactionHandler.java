package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionHandler implements TransactionTypeHandler {
    private AccountService accountService;

    @Autowired
    public PaymentTransactionHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }

    @Override
    public void handle(Transaction tx) {
        if (tx.getAmount() > 0) {
            Operation op = new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), tx.getAmount());
            accountService.transferMoney(tx.getSender(), tx.getRecipient(), op);
        }
    }
}
