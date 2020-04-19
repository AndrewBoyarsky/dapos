package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import org.springframework.stereotype.Component;

@Component
public class FeeProviderTransactionHandler implements TransactionTypeHandler {

    @Override
    public TxType type() {
        return TxType.SET_FEE_PROVIDER;
    }

    @Override
    public void handle(Transaction tx) {
//        FeeProviderAttachment feeProviderAttachment = (FeeProviderAttachment) attachment;

    }
}
