package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

public interface TransactionTypeHandler {

    TxType type();

    void handle(Transaction tx, AbstractAttachment attachment);
}
