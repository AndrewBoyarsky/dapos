package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.stereotype.Component;

@Component
public class StartValidatorGasCalculator implements GasCalculator {
    @Override
    public int gasRequired(Transaction tx) {
        return 5000;
    }

    @Override
    public TxType type() {
        return TxType.START_VALIDATOR;
    }
}
