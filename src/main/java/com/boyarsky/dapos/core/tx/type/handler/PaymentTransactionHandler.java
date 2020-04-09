package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionHandler implements TransactionTypeHandler {
    @Autowired
    private AccountService accountService;
    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }

    @Override
    public void handle(Transaction tx) {
        long amount = tx.getAmount();
        AccountId recipient = tx.getRecipient();
        AccountId sender = tx.getSender();
        Account recAccount = accountService.get(recipient);
        Account senderAccount = accountService.get(sender);
        recAccount.setBalance(recAccount.getBalance() + amount);
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        accountService.save(recAccount);
        accountService.save(senderAccount);
    }
}
