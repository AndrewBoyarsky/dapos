package com.boyarsky.dapos.core.tx.type;

import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BlockchainLogicVerifierTest {

    @Test
    void doValidate_Ok() {
        Map<TxType, GasCalculator> gas = mockAll(GasCalculator.class);
        Map<TxType, TransactionTypeHandler> handlers = mockAll(TransactionTypeHandler.class);
        Map<TxType, TransactionTypeValidator> validators = mockAll(TransactionTypeValidator.class);

        BlockchainLogicVerifier verifier = new BlockchainLogicVerifier(new ArrayList<>(gas.values()), new ArrayList<>(validators.values()), new ArrayList<>(handlers.values()));

        verifier.doValidate();

        assertEquals(gas, verifier.gas());
        assertEquals(handlers, verifier.handlers());
        assertEquals(validators, verifier.validators());
    }


    @Test
    void doValidate_duplicate() {
        Map<TxType, GasCalculator> gas = mockAll(GasCalculator.class);
        List<TransactionTypeValidator> validators = List.of(mockForType(TransactionTypeValidator.class, TxType.PAYMENT), mockForType(TransactionTypeValidator.class, TxType.PAYMENT));

        BlockchainLogicVerifier verifier = new BlockchainLogicVerifier(new ArrayList<>(gas.values()), validators, List.of());

        RuntimeException runtimeException = assertThrows(RuntimeException.class, verifier::doValidate);

        String message = runtimeException.getMessage();
        assertTrue(message.contains("validator") && message.contains(TxType.PAYMENT.toString()));
    }

    @Test
    void doValidate_emptyTypes() {
        Map<TxType, GasCalculator> gas = mockAll(GasCalculator.class);
        List<TransactionTypeValidator> validators = List.of(mockForType(TransactionTypeValidator.class, TxType.ALL), mockForType(TransactionTypeValidator.class, TxType.PAYMENT));

        BlockchainLogicVerifier verifier = new BlockchainLogicVerifier(new ArrayList<>(gas.values()), validators, List.of());

        RuntimeException runtimeException = assertThrows(RuntimeException.class, verifier::doValidate);

        String message = runtimeException.getMessage();
        assertTrue(message.contains("validator") && message.contains("defined"));
    }

    private <T extends TypedComponent> Map<TxType, T> mockAll(Class<T> clss) {
        Map<TxType, T> map = new HashMap<>();
        for (TxType value : TxType.values()) {
            map.put(value, mockForType(clss, value));
        }
        return map;
    }

    private <T extends TypedComponent> T mockForType(Class<T> clazz, TxType type) {
        T mock = mock(clazz);
        doReturn(type).when(mock).type();
        return mock;
    }

}