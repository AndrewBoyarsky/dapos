package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidatorTxHandler implements TransactionTypeHandler {
    private final ValidatorService validatorService;

    @Autowired
    public ValidatorTxHandler(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        RegisterValidatorAttachment attachment = tx.getAttachment(RegisterValidatorAttachment.class);
        validatorService.registerValidator(attachment.getPublicKey(), attachment.getFee(), attachment.getRewardId(), attachment.isEnable(), tx.getHeight());
    }

    @Override
    public TxType type() {
        return TxType.REGISTER_VALIDATOR;
    }
}
