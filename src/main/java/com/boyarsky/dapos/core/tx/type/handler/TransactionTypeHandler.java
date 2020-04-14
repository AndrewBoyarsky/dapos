package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxHandlingException;
import com.boyarsky.dapos.core.tx.type.TxType;

public interface TransactionTypeHandler {

    TxType type();

    void handle(Transaction tx) throws TxHandlingException;
}
