package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxTypedComponent;

public interface TransactionTypeHandler extends TxTypedComponent {
    void handle(Transaction tx) throws TxHandlingException;
}
