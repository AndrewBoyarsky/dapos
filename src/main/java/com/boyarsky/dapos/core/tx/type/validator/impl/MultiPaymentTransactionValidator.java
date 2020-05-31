package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiPaymentTransactionValidator implements TransactionTypeValidator {
    private final AccountService accountService;

    @Autowired
    public MultiPaymentTransactionValidator(AccountService service) {
        this.accountService = service;
    }

    @Override
    public void validate(Transaction tx) {
        MultiAccountAttachment attachment = tx.getAttachment(MultiAccountAttachment.class);
        Account account = accountService.get(tx.getSender());
        MultiSendValidationUtil.commonValidation(tx, attachment, account.getBalance(), 1000);
    }

    @Override
    public TxType type() {
        return TxType.MULTI_PAYMENT;
    }
}
