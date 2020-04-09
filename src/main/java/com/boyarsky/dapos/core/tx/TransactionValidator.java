package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.utils.CryptoUtils;
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
    }

    public ValidationResult validate(Transaction tx) {
        TransactionTypeValidator defaultValidator = validators.get(TxType.ALL);
        if (defaultValidator != null) {
            try {
                defaultValidator.validate(tx);
            } catch (TransactionTypeValidator.TxNotValidException e) {
                return new ValidationResult("Invalid transaction (general validation does not pass):" + e.getMessage(), -1, tx, e);
            } catch (Exception e) {
                return new ValidationResult("Unknown general validation error:" + e.getMessage(), -2, tx, e);
            }
        } else {
            log.warn("NO DEFAULT TX VALIDATOR DEFINED");
        }
        TxType txType = tx.getType();
        TransactionTypeValidator validator = validators.get(txType);

        if (validator == null) {
            return new ValidationResult("Validator not exist for type" + txType, -5, tx, null);
        }
        try {
            validator.validate(tx);
        } catch (TransactionTypeValidator.TxNotValidException e) {
            return new ValidationResult("Incorrect tx of type " + txType + " Details: " + e.getMessage(), e.getCode(), tx, e);
        } catch (Exception e) {
            return new ValidationResult("Unknown runtime error during tx validation " + e.getMessage(), -9, tx, e);
        }
        return new ValidationResult("Passed", 0, tx, null);
    }
}
