package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.stereotype.Component;

@Component
public class UpdateValidatorTransactionValidator implements TransactionTypeValidator {
    private ValidatorService validatorService;

    public UpdateValidatorTransactionValidator(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {

    }

    @Override
    public TxType type() {
        return TxType.UPDATE_VALIDATOR;
    }
}
