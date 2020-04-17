package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransactionHandler implements TransactionTypeHandler {
    private final AccountService accountService;

    @Autowired
    public DefaultTransactionHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }

    @Override
    public void handle(Transaction tx) {
        AccountId sender = tx.getSender();
        accountService.assignPublicKey(sender, tx.getSenderPublicKey());
        if (tx.getRecipient() != null && tx.getAmount() > 0) {
            accountService.transferMoney(sender, tx.getRecipient(), tx.getAmount());
        }
        accountService.transferMoney(sender, null, tx.getFee());
    }
}
