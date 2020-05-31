package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.stereotype.Component;

@Component
public class MultiCurrencyTransferGasCalculator implements GasCalculator {

    @Override
    public int gasRequired(Transaction tx) {
        MultiAccountAttachment attachment = tx.getAttachment(MultiAccountAttachment.class);
        return 300 + attachment.getTransfers().size() * 120;
    }

    @Override
    public TxType type() {
        return TxType.MULTI_CURRENCY_TRANSFER;
    }
}
