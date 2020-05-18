package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.stereotype.Component;

@Component
public class VoteTransactionHandler implements TransactionTypeHandler {
    @Override
    public void handle(Transaction tx) throws TxHandlingException {

    }

    @Override
    public TxType type() {
        return TxType.VOTE;
    }
}
