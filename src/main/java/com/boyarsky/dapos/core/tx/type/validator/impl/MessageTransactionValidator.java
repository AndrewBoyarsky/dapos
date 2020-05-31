package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageTransactionValidator implements TransactionTypeValidator {
    private AccountService accountService;

    @Autowired
    public MessageTransactionValidator(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void validate(Transaction tx) {
        MessageAttachment message = tx.getAttachment(MessageAttachment.class);
        if (!message.isToSelf()) {
            if (tx.getRecipient() == null) {
                throw new TxNotValidException("Recipient should be present in tx for DH encrypted message", null, tx, ErrorCodes.RECIPIENT_NULL_DH);
            }
            Account account = accountService.get(tx.getRecipient());
            if (account == null) {
                throw new TxNotValidException("Recipient account missing in database", null, tx, ErrorCodes.RECIPIENT_DB_MISSING);
            }
            if (account.getPublicKey() == null) {
                throw new TxNotValidException("Recipient account has not public key", null, tx, ErrorCodes.RECIPIENT_PK_MISSING);
            }
        }
    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
