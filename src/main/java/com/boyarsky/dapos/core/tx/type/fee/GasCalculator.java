package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;

public interface GasCalculator {
    TxType type();

    long gasRequired(Transaction tx);
}
