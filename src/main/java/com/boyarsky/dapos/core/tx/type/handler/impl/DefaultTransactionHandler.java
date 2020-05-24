package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.account.Operation;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.service.message.MessageService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransactionHandler implements TransactionTypeHandler {
    private final AccountService accountService;
    private final FeeProviderService feeProviderService;
    private final MessageService messageService;

    @Autowired
    public DefaultTransactionHandler(AccountService accountService, FeeProviderService feeProviderService, MessageService messageService) {
        this.accountService = accountService;
        this.feeProviderService = feeProviderService;
        this.messageService = messageService;
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }

    @Override
    public void handle(Transaction tx) {
        AccountId sender = tx.getSender();
        accountService.assignPublicKey(sender, tx.getSenderPublicKey());
        if (tx.getAmount() > 0 && !tx.isVal()) {
            Operation op = new Operation(tx.getTxId(), tx.getHeight(), tx.getType().toString(), tx.getAmount());
            accountService.transferMoney(sender, tx.getRecipient(), op);
        }
        NoFeeAttachment nofee = tx.getAttachment(NoFeeAttachment.class);
        if (nofee != null) {
            feeProviderService.charge(nofee.getPayer(), tx.getHeight(), tx.getFee(), tx.getSender(), tx.getRecipient(), tx.getTxId());
        } else {
            Operation op = new Operation(tx.getTxId(), tx.getHeight(), "Tx Fee", tx.getFee());
            accountService.transferMoney(sender, null, op);
        }
        MessageAttachment messageAttachment = tx.getAttachment(MessageAttachment.class);
        if (messageAttachment != null && tx.getType() != TxType.MESSAGE) {
            messageService.handle(messageAttachment, tx);
        }
    }
}
