package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;


public class PaymentGasCalculator implements GasCalculator {

    @Override
    public TxType type() {
        return TxType.PAYMENT;
    }

    @Override
    public long gasRequired(Transaction tx) {
        return 0;
    }
}
