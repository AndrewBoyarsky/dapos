package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.stereotype.Component;

@Component
public class VoteTransactionValidator implements TransactionTypeValidator {
    private ValidatorService service;

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getRecipient() == null) {
            throw new TxNotValidException("Validator address should be set as recipient for vote tx, got 'null'", null, tx, ErrorCodes.VOTE_VALIDATOR_NOT_SPECIFIED);
        }
        ValidatorEntity validator = service.get(tx.getRecipient());
        if (validator == null) {
            throw new TxNotValidException("Validator for address " + tx.getRecipient() + " was not found", null, tx, ErrorCodes.VOTE_VALIDATOR_NOT_FOUND);
        }
        if (!validator.isEnabled()) {
            throw new TxNotValidException("Validator " + validator.getId() + " is disabled", null, tx, ErrorCodes.VOTE_VALIDATOR_DISABLED);
        }

    }

    @Override
    public TxType type() {
        return TxType.VOTE;
    }
}
