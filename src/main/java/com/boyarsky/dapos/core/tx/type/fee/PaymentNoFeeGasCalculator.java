package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.Transaction;

public class PaymentNoFeeGasCalculator implements GasCalculator {

    @Override
    public TxType type() {
//        return TxType.PAYMENT_NO_FEE;
        return null;
    }

    @Override
    public long gasRequired(Transaction tx) {
        byte[] data = tx.getData();
        return data.length;
    }
}
