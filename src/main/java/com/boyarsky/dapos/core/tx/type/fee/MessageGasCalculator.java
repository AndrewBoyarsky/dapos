package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.stereotype.Component;

@Component
public class MessageGasCalculator implements GasCalculator {

    @Override
    public int gasRequired(Transaction tx) {
        return 200;
    }

    @Override
    public TxType type() {
        return TxType.MESSAGE;
    }
}
