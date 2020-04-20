package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeeProviderTransactionHandler implements TransactionTypeHandler {

    private FeeProviderService service;

    @Autowired
    public FeeProviderTransactionHandler(FeeProviderService service) {
        this.service = service;
    }

    @Override
    public TxType type() {
        return TxType.SET_FEE_PROVIDER;
    }

    @Override
    public void handle(Transaction tx) {
        FeeProviderAttachment attachment = tx.getAttachment(FeeProviderAttachment.class);
        service.handle(attachment, tx);
    }
}
