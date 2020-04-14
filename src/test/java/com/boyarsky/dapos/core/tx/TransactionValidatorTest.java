package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {


    @Mock
    TransactionTypeValidator validator1;
    @Mock
    TransactionTypeValidator validator2;
    @Mock
    TransactionTypeValidator validator3;
    TransactionValidator validator;

    @Test
    void create_duplicateValidators() {
        doReturn(TxType.CURRENCY).when(validator1).type();
        doReturn(TxType.CURRENCY).when(validator2).type();
        doReturn(TxType.PAYMENT).when(validator3).type();
        assertThrows(RuntimeException.class, () -> new TransactionValidator(List.of(validator1, validator2, validator3)));
    }

    @Test
    void create_noDefaultValidator() {
        doReturn(TxType.CLAIM).when(validator1).type();
        doReturn(TxType.CURRENCY).when(validator2).type();

        assertThrows(RuntimeException.class, () -> new TransactionValidator(List.of(validator1, validator2)));
    }

    @Test
    void validate_Ok() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.CONTRACT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.CONTRACT).when(validator2).type();
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(0, result.getCode());
        verify(validator1).validate(tx);
        verify(validator2).validate(tx);
    }

    @Test
    void validate_typeValidatorIsNotRegistered() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.CONTRACT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.CLAIM).when(validator2).type();
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(-1, result.getCode());
        verify(validator1, never()).validate(tx);
        verify(validator2, never()).validate(tx);
    }

    @Test
    void validate_defaultValidatorThrowPredictableEx() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.PAYMENT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.PAYMENT).when(validator2).type();
        doThrow(new TransactionTypeValidator.TxNotValidException("Invalid test tx", tx, -22, null)).when(validator1).validate(tx);
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(-22, result.getCode());
        verify(validator1).validate(tx);
        verify(validator2, never()).validate(tx);
    }

    @Test
    void validate_defaultValidatorThrowUnpredictableEx() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.PAYMENT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.PAYMENT).when(validator2).type();
        doThrow(new RuntimeException("Fatal error")).when(validator1).validate(tx);
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(-2, result.getCode());
        assertEquals(RuntimeException.class, result.getE().getClass());
        verify(validator1).validate(tx);
        verify(validator2, never()).validate(tx);
    }

    @Test
    void validate_validatorThrowPredictableEx() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.PAYMENT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.PAYMENT).when(validator2).type();
        doThrow(new TransactionTypeValidator.TxNotValidException("Invalid tx of test type", tx, -100)).when(validator2).validate(tx);
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(-100, result.getCode());
        verify(validator1).validate(tx);
    }

    @Test
    void validate_validatorThrowUnpredictableEx() {
        Transaction tx = mock(Transaction.class);
        doReturn(TxType.PAYMENT).when(tx).getType();
        doReturn(TxType.ALL).when(validator1).type();
        doReturn(TxType.PAYMENT).when(validator2).type();
        doThrow(new RuntimeException("Fatal error")).when(validator2).validate(tx);
        TransactionValidator v = new TransactionValidator(List.of(validator1, validator2));

        ProcessingResult result = v.validate(tx);

        assertEquals(-9, result.getCode());
        assertEquals(RuntimeException.class, result.getE().getClass());
        verify(validator1).validate(tx);
    }


}