package com.boyarsky.dapos.core.tx.type.fee;

import com.boyarsky.dapos.core.tx.ErrorCode;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TxException;

public class GasCalculationException extends TxException {
    private long usedGas;

    public GasCalculationException(String message, Throwable cause, Transaction tx, ErrorCode code, long usedGas) {
        super(message, cause, tx, code);
        this.usedGas = usedGas;
    }

    public long getUsedGas() {
        return usedGas;
    }
}
