package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorControlAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartValidatorTxHandler implements TransactionTypeHandler {
    private ValidatorService service;

    @Autowired
    public StartValidatorTxHandler(ValidatorService service) {
        this.service = service;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        ValidatorControlAttachment attachment = tx.getAttachment(ValidatorControlAttachment.class);
        service.toggleValidator(attachment.getValidatorId(), true, tx.getHeight());
    }

    @Override
    public TxType type() {
        return TxType.START_VALIDATOR;
    }
}
