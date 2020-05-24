package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteTransactionHandler implements TransactionTypeHandler {
    private final ValidatorService service;

    @Autowired
    public VoteTransactionHandler(ValidatorService service) {
        this.service = service;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        VoteAttachment attachment = tx.getAttachment(VoteAttachment.class);
        service.addVote(tx.getRecipient(), tx.getSender(), tx.getAmount(), tx.getHeight());
    }

    @Override
    public TxType type() {
        return TxType.VOTE;
    }
}
