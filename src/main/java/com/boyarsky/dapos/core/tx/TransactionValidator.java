package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransactionValidator {
    private final Map<TxType, TransactionTypeValidator> validators = new HashMap<>();

    @Autowired
    public TransactionValidator(List<TransactionTypeValidator> validators) {
        Map<TxType, List<TransactionTypeValidator>> validatorMap = validators.stream().collect(Collectors.groupingBy(TransactionTypeValidator::type));
        validatorMap.forEach((t, l) -> {
            if (l.size() > 1) {
                throw new RuntimeException(l.size() + " validators registered for transaction of type " + t + " :" + l);
            }
            this.validators.put(t, l.get(0));
        });
        TransactionTypeValidator defaultValidator = this.validators.get(TxType.ALL);
        if (defaultValidator == null) {
            throw new RuntimeException("Default validator is not registered");
        }
    }

    public ProcessingResult validate(Transaction tx) {
        TxType txType = tx.getType();
        TransactionTypeValidator validator = validators.get(txType);

        if (validator == null) {
            return new ProcessingResult("Validator not exist for type" + txType, -1, tx, null);
        }
        TransactionTypeValidator defaultValidator = validators.get(TxType.ALL);
        try {
            defaultValidator.validate(tx);
        } catch (TransactionTypeValidator.TxNotValidException e) {
            return new ProcessingResult("Invalid transaction (general validation does not pass):" + e.getMessage(), e.getCode(), tx, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown general validation error:" + e.getMessage(), -2, tx, e);
        }

        try {
            validator.validate(tx);
        } catch (TransactionTypeValidator.TxNotValidException e) {
            return new ProcessingResult("Incorrect tx of type " + txType + " Details: " + e.getMessage(), e.getCode(), tx, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown runtime error during tx validation " + e.getMessage(), -9, tx, e);
        }
        return new ProcessingResult("Passed", 0, tx, null);
    }
}
