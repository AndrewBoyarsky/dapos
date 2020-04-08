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
    }

    public int validate(Transaction tx) {
        int type = tx.getType();
        TxType txType;
        try {
            txType = TxType.ofCode(type);
        } catch (IllegalArgumentException e) {
            log.error("Unknown tx type for " + tx, e);
            return -1;
        }
        TransactionTypeValidator validator = validators.get(txType);
        if (validator == null) {
            return -2;
        }
        try {
            validator.validate(tx);
        } catch (TransactionTypeValidator.TxNotValidException e) {
            log.error("Invalid tx " + tx, e);
            return e.getCode();
        } catch (Exception e) {
            log.error("Unknown runtime error during tx validation: tx " + tx, e);
            return -100;
        }
        return 0;
    }
}
