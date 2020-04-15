package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TransactionValidator {
    private final Map<TxType, TransactionTypeValidator> validators = new HashMap<>();

    @Autowired
    public TransactionValidator(Map<TxType, TransactionTypeValidator> validators) {
        this.validators.putAll(validators);
    }

    public void validate(Transaction tx) throws TxNotValidException {
        TxType txType = tx.getType();
        TransactionTypeValidator defaultValidator = validators.get(TxType.ALL);
        defaultValidator.validate(tx);
        TransactionTypeValidator validator = validators.get(txType);
        validator.validate(tx);
    }
}
