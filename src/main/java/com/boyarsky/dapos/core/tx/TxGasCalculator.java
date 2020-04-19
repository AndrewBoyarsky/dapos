package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculationException;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TxGasCalculator {
    private final Map<TxType, GasCalculator> calculators = new HashMap<>();

    @Autowired
    public TxGasCalculator(Map<TxType, GasCalculator> calculators) {
        this.calculators.putAll(calculators);
    }

    public int calculateGas(Transaction tx) throws GasCalculationException {
        int gas = this.calculators.get(TxType.ALL).gasRequired(tx);
        GasCalculator gasCalculator = this.calculators.get(tx.getType());
        gas += gasCalculator.gasRequired(tx);
        return gas;
    }
}
