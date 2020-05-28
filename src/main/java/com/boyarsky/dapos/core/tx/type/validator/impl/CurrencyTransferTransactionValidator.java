package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyTransferTransactionValidator implements TransactionTypeValidator {
    private CurrencyService service;

    @Autowired
    public CurrencyTransferTransactionValidator(CurrencyService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        CurrencyIdAttachment attachment = tx.getAttachment(CurrencyIdAttachment.class);
        CurrencyHolder holder = service.getCurrencyHolder(tx.getSender(), attachment.getCurrencyId());
        if (holder == null) {
            throw new TxNotValidException("Account '" + tx.getSender().getAppSpecificAccount() + "' has no currency " + attachment.getCurrencyId(), null, tx, ErrorCodes.CURRENCY_TRANSFER_NO_CURRENCY);
        }
        if (holder.getAmount() < tx.getAmount()) {
            throw new TxNotValidException("Account '" + tx.getSender().getAppSpecificAccount() + "' has not enough currency to transfer, wanted " + tx.getAmount() + " got " + holder.getAmount(), null, tx, ErrorCodes.CURRENCY_TRANSFER_NOT_ENOUGH_CURRENCY);
        }
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_TRANSFER;
    }
}
