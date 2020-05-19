package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.validator.StakeholderService;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RevokeTransactionValidator implements TransactionTypeValidator {
    private ValidatorService service;
    private StakeholderService stakeholderService;

    @Autowired
    public RevokeTransactionValidator(ValidatorService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getRecipient() == null) {
            throw new TxNotValidException("Validator address should be specified as recipient, got 'null'", null, tx, ErrorCodes.REVOKE_VALIDATOR_NOT_SPECIFIED);
        }
        if (!tx.getRecipient().isVal()) {
            throw new TxNotValidException("Validator address does not belong to type 'validator', got ", null, tx, ErrorCodes.REVOKE_VALIDATOR_INCORRECT_ADDRESS_TYPE);
        }
        ValidatorEntity validator = service.get(tx.getRecipient());
        if (validator == null) {
            throw new TxNotValidException("Validator not found by address " + tx.getRecipient(), null, tx, ErrorCodes.REVOKE_UNKNOWN_VALIDATOR);
        }
        if (!stakeholderService.exists(validator.getId(), tx.getSender())) {
            throw new TxNotValidException("Voter " + tx.getSender() + " has no votes for validator " + validator.getId(), null, tx, ErrorCodes.REVOKE_VOTE_FOR_VALIDATOR_NOT_EXIST);
        }
    }

    @Override
    public TxType type() {
        return TxType.REVOKE;
    }
}
