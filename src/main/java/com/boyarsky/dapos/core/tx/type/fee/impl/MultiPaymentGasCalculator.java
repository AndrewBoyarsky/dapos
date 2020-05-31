package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.stereotype.Component;

@Component
public class MultiPaymentGasCalculator implements GasCalculator {

    @Override
    public int gasRequired(Transaction tx) {
        MultiAccountAttachment attachment = tx.getAttachment(MultiAccountAttachment.class);
        return 250 + attachment.getTransfers().size() * 100;
    }

    @Override
    public TxType type() {
        return TxType.MULTI_PAYMENT;
    }
}
