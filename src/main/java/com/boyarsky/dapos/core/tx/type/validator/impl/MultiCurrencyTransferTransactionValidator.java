package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyMultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiCurrencyTransferTransactionValidator implements TransactionTypeValidator {
    private CurrencyService service;

    @Autowired
    public MultiCurrencyTransferTransactionValidator(CurrencyService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) {
        CurrencyMultiAccountAttachment attachment = tx.getAttachment(CurrencyMultiAccountAttachment.class);
        CurrencyHolder currencyHolder = service.getCurrencyHolder(tx.getSender(), attachment.getCurrencyId());
        if (currencyHolder == null) {
            throw new TxNotValidException("Account does not hold currency " + attachment.getCurrencyId(), null, tx, ErrorCodes.MULTI_SEND_NO_CURRENCY_ON_ACCOUNT);
        }
        MultiSendValidationUtil.commonValidation(tx, attachment, currencyHolder.getAmount(), 800);
    }

    @Override
    public TxType type() {
        return TxType.MULTI_CURRENCY_TRANSFER;
    }
}
