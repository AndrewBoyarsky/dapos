package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.message.MessageService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageTxHandler implements TransactionTypeHandler {
    private final MessageService service;

    @Autowired
    public MessageTxHandler(MessageService service) {
        this.service = service;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        MessageAttachment attachment = tx.getAttachment(MessageAttachment.class);
        service.handle(attachment, tx);
    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
