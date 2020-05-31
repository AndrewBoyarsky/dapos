package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {
    @Mock
    TransactionTypeValidator validator1;
    @Mock
    TransactionTypeValidator validator2;
    @Mock
    Transaction tx;

    @Test
    void validate_Ok() {
        doReturn(TxType.PAYMENT).when(tx).getType();
        TransactionValidator v = new TransactionValidator(Map.of(TxType.PAYMENT, validator1, TxType.ALL, validator2));

        v.validate(tx);

        verify(validator1).validate(tx);
        verify(validator2).validate(tx);
    }
}