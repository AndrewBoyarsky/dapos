package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RevokeTransactionHandler implements TransactionTypeHandler {
    ValidatorService validatorService;

    @Autowired
    public RevokeTransactionHandler(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        validatorService.revoke(tx.getRecipient(), tx.getSender(), tx.getHeight());
    }

    @Override
    public TxType type() {
        return TxType.REVOKE;
    }
}
