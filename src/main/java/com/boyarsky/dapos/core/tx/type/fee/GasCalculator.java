package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxTypedComponent;

public interface GasCalculator extends TxTypedComponent {
    int gasRequired(Transaction tx);
}
