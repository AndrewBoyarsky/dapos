package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxHandlingException;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.stereotype.Component;

@Component
public class ClaimTransactionHandler implements TransactionTypeHandler {
    @Override
    public void handle(Transaction tx) throws TxHandlingException {

    }

    @Override
    public TxType type() {
        return TxType.CLAIM;
    }
}
