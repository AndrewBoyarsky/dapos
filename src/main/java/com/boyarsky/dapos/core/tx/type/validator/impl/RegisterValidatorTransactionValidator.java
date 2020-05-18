package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegisterValidatorTransactionValidator implements TransactionTypeValidator {
    private ValidatorService service;

    @Autowired
    public RegisterValidatorTransactionValidator(ValidatorService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        RegisterValidatorAttachment attachment = tx.getAttachment(RegisterValidatorAttachment.class);
        if (attachment.getFee() < 0 || attachment.getFee() > 10000) {
            throw new TxNotValidException();
        }
        AccountId validatorId = new AccountId(CryptoUtils.validatorAddress(attachment.getPublicKey()));
        if (service.get(validatorId) != null) {
            throw new TxNotValidException();
        }
    }

    @Override
    public TxType type() {
        return TxType.REGISTER_VALIDATOR;
    }
}
