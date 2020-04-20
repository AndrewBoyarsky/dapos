package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultGasCalculator implements GasCalculator {
    static final int BASE_TX_GAS = 1000;
    MessageGasCalculator calculator;

    @Autowired
    public DefaultGasCalculator(MessageGasCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }

    @Override
    public int gasRequired(Transaction tx) {
        int gas = BASE_TX_GAS;
        if (tx.isFirst()) {
            gas += 100;
        }
        if (tx.getRecipient() != null) {
            gas += 50;
        }
        if (tx.getAmount() != 0) {
            gas += 20;
        }
        Attachment attachment = tx.getAttachment(NoFeeAttachment.class);
        if (attachment != null) {
            gas += 25;
        }
        MessageAttachment messageAttachment = tx.getAttachment(MessageAttachment.class);
        if (messageAttachment != null && tx.getType() != TxType.MESSAGE) {
            gas += calculator.gasRequired(tx);
        }
        gas += tx.getData().length * 3;
        return gas;
    }
}
