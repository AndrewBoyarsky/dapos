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
public class StopValidatorTransactionValidator implements TransactionTypeValidator {
    private ValidatorService validatorService;

    public StopValidatorTransactionValidator(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
//        ValidatorControlAttachment attachment = tx.getAttachment(ValidatorControlAttachment.class);
        ValidatorEntity entity = validatorService.get(tx.getSender());
        if (entity == null) {
            throw new TxNotValidException("Validator " + tx.getSender() + " was not found", null, tx, ErrorCodes.NOT_FOUND_VALIDATOR);
        }
        if (!entity.getRewardId().equals(tx.getSender())) {
            throw new TxNotValidException("Only reward account can stop the validator, got " + tx.getSender() + ", expected " + entity.getRewardId(), null, tx, ErrorCodes.ACCOUNT_NOT_ELIGIBLE_TO_CONTROL_VALIDATOR);
        }
        if (!entity.isEnabled()) {
            throw new TxNotValidException("Validator " + entity.getId() + " is already down", null, tx, ErrorCodes.VALIDATOR_ALREADY_DOWN);
        }
    }

    @Override
    public TxType type() {
        return TxType.STOP_VALIDATOR;
    }
}
