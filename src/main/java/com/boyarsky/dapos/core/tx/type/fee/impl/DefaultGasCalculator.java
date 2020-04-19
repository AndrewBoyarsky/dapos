package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.stereotype.Component;

@Component
public class DefaultGasCalculator implements GasCalculator {
    static final int BASE_TX_GAS = 1000;

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
        gas += tx.getData().length * 3;
        return gas;
    }
}
