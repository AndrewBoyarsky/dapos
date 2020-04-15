package com.boyarsky.dapos.core.tx.type;

import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class BlockchainLogicVerifier {
    private final List<GasCalculator> gasCalculatorList;
    private final List<TransactionTypeValidator> validators;
    private final List<TransactionTypeHandler> handlers;

    @Autowired
    public BlockchainLogicVerifier(List<GasCalculator> gasCalculatorList, List<TransactionTypeValidator> validators, List<TransactionTypeHandler> handlers) {
        this.gasCalculatorList = gasCalculatorList;
        this.validators = validators;
        this.handlers = handlers;
    }

    @PostConstruct
    public void doValidate() {
        validate(gasCalculatorList, "Gas calculator");
        validate(validators, "Tx validator");
        validate(handlers, "Tx handler");
    }

    private <T extends TypedComponent> void validate(List<T> entities, String name) {
        Map<TxType, List<T>> map = entities.stream().collect(Collectors.groupingBy(T::type));
        map.forEach((t, l) -> {
            if (l.size() > 1) {
                throw new RuntimeException(l.size() + " " + name + "s registered for transaction of type " + t + " :" + l);
            }
        });
        for (TxType value : TxType.values()) {
            if (map.get(value) == null) {
                throw new RuntimeException("No " + name + " defined for type " + value);
            }
        }
    }

    private <T extends TypedComponent> Map<TxType, T> getEntityMap(List<T> entities) {
        return entities.stream().collect(Collectors.toMap(T::type, Function.identity()));
    }

    @Bean
    Map<TxType, GasCalculator> gas() {
        return getEntityMap(gasCalculatorList);
    }

    @Bean
    Map<TxType, TransactionTypeHandler> handlers() {
        return getEntityMap(handlers);
    }

    @Bean
    Map<TxType, TransactionTypeValidator> validators() {
        return getEntityMap(validators);
    }
}
