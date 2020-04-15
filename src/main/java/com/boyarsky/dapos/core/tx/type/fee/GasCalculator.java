package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TypedComponent;

public interface GasCalculator extends TypedComponent {
    long gasRequired(Transaction tx);
}
