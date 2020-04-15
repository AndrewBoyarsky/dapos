package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.springframework.stereotype.Component;

@Component
public class PaymentNoFeeGasCalculator implements GasCalculator {

    @Override
    public TxType type() {
        return TxType.SET_FEE_PROVIDER;
    }

    @Override
    public int gasRequired(Transaction tx) {
        byte[] data = tx.getData();
        return data.length;
    }
}
