package com.boyarsky.dapos.core.tx.type.handler;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxHandlingException;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.MessageAttachment;

public class MessageTxHandler implements TransactionTypeHandler {

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        MessageAttachment attachment = tx.getAttachment(MessageAttachment.class);

    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
