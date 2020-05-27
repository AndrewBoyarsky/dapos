package com.boyarsky.dapos.core.tx.type.fee.impl;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.stereotype.Component;

@Component
public class CurrencyTransferGasCalculator implements GasCalculator {

    @Override
    public int gasRequired(Transaction tx) {
        return 100;
    }

    @Override
    public TxType type() {
        return TxType.CURRENCY_ISSUANCE;
    }
}
