package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
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
    private final LedgerService ledgerService;
    private final FeeProviderService feeProviderService;
    private final MessageService messageService;

    @Autowired
    public DefaultTransactionHandler(AccountService accountService, FeeProviderService feeProviderService, MessageService messageService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.feeProviderService = feeProviderService;
        this.messageService = messageService;
        this.ledgerService = ledgerService;
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }

    @Override
    public void handle(Transaction tx) {
        AccountId sender = tx.getSender();
        accountService.assignPublicKey(sender, tx.getSenderPublicKey());
        if (tx.getAmount() > 0) {
            accountService.transferMoney(sender, tx.getRecipient(), tx.getAmount());
        }
        NoFeeAttachment nofee = tx.getAttachment(NoFeeAttachment.class);
        LedgerRecord record = new LedgerRecord();
        record.setAmount(tx.getAmount());
        record.setId(tx.getTxId());
        record.setHeight(tx.getHeight());
        record.setSender(tx.getSender());
        record.setRecipient(tx.getRecipient());
        record.setType(tx.getType());
        if (nofee != null) {
            feeProviderService.charge(nofee.getPayer(), tx.getFee(), tx.getSender(), tx.getRecipient());
            record.setFee(0);
        } else {
            accountService.transferMoney(sender, null, tx.getFee());
            record.setFee(tx.getFee());
        }
        ledgerService.add(record);
        MessageAttachment messageAttachment = tx.getAttachment(MessageAttachment.class);
        if (messageAttachment != null && tx.getType() != TxType.MESSAGE) {
            messageService.handle(messageAttachment, tx);
        }
    }
}
