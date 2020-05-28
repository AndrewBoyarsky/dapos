package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyIssueTransactionValidator implements TransactionTypeValidator {
    @Autowired
    private CurrencyService service;

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getRecipient() != null) {
            throw new TxNotValidException("Recipient should not be specified for currency issuance", null, tx, ErrorCodes.CURRENCY_ISSUANCE_HAS_RECIPIENT);
        }
        CurrencyIssuanceAttachment attachment = tx.getAttachment(CurrencyIssuanceAttachment.class);
        if (service.reserved(attachment.getCode())) {
            throw new TxNotValidException("Currency code '" + attachment.getCode() + "' was already reserved", null, tx, ErrorCodes.CURRENCY_ISSUANCE_CODE_ALREADY_RESERVED);
        }
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_ISSUANCE;
    }
}
