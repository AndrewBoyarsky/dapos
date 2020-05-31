package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.currency.Currency;
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
public class CurrencyClaimTransactionValidator implements TransactionTypeValidator {
    private CurrencyService service;

    @Autowired
    public CurrencyClaimTransactionValidator(CurrencyService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) {
        CurrencyIdAttachment attachment = tx.getAttachment(CurrencyIdAttachment.class);
        if (tx.getRecipient() != null) {
            throw new TxNotValidException("Claim currency reserve tx does not allow recipient", null, tx, ErrorCodes.CURRENCY_CLAIM_RECIPIENT_NOT_ALLOWED);
        }
        CurrencyHolder holder = service.getCurrencyHolder(tx.getSender(), attachment.getCurrencyId());
        if (holder == null) {
            throw new TxNotValidException("Account '" + tx.getSender().getAppSpecificAccount() + "' has no currency " + attachment.getCurrencyId(), null, tx, ErrorCodes.CURRENCY_CLAIM_NO_CURRENCY);
        }
        if (holder.getAmount() < tx.getAmount()) {
            throw new TxNotValidException("Account '" + tx.getSender().getAppSpecificAccount() + "' has not enough currency to claim reserve, wanted " + tx.getAmount() + " got " + holder.getAmount(), null, tx, ErrorCodes.CURRENCY_CLAIM_NOT_ENOUGH_CURRENCY);
        }
        Currency currency = service.getById(attachment.getCurrencyId());
        if (currency.getReserve() == 0 && currency.getSupply() != tx.getAmount()) {
            throw new TxNotValidException("Cannot liquidate currency '" + currency.getCode() + "' with zero reserve partially, sender should own the whole currency supply " + currency.getSupply() + ", but has only " + tx.getAmount(), null, tx, ErrorCodes.CURRENCY_CLAIM_ZERO_RESERVE);
        }
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_CLAIM_RESERVE;
    }
}
