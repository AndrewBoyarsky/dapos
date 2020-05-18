package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorControlAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.stereotype.Component;

@Component
public class StartValidatorTransactionValidator implements TransactionTypeValidator {
    private ValidatorService validatorService;

    public StartValidatorTransactionValidator(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        ValidatorControlAttachment attachment = tx.getAttachment(ValidatorControlAttachment.class);
        ValidatorEntity entity = validatorService.get(attachment.getValidatorId());
        if (entity == null) {
            throw new TxNotValidException("Validator " + attachment.getValidatorId() + " was not found", null, tx, ErrorCodes.NOT_FOUND_VALIDATOR);
        }
        if (!entity.getRewardId().equals(tx.getSender())) {
            throw new TxNotValidException("Only reward account can start the validator, got " + tx.getSender() + ", expected " + entity.getRewardId(), null, tx, ErrorCodes.ACCOUNT_NOT_ELIGIBLE_TO_CONTROL_VALIDATOR);
        }
        if (entity.isEnabled()) {
            throw new TxNotValidException("Validator " + entity.getId() + " is already enabled", null, tx, ErrorCodes.VALIDATOR_ALREADY_UP);
        }
    }

    @Override
    public TxType type() {
        return TxType.START_VALIDATOR;
    }
}
