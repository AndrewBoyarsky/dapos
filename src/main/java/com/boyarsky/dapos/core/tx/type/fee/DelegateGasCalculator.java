package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.stereotype.Component;

@Component
public class DelegateGasCalculator implements GasCalculator {

    @Override
    public TxType type() {
        return TxType.DELEGATE;
    }

    @Override
    public int gasRequired(Transaction tx) {
        return 0;
    }

}
