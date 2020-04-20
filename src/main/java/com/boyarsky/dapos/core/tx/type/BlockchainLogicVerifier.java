package com.boyarsky.dapos.core.tx.type;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;
import com.boyarsky.dapos.core.tx.type.attachment.AttachmentTypedComponent;
import com.boyarsky.dapos.core.tx.type.attachment.IndependentAttachmentType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculator;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.parser.AttachmentTxTypeParser;
import com.boyarsky.dapos.core.tx.type.parser.IndependentAttachmentParser;
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
    private final List<AttachmentTxTypeParser<? extends Attachment>> txTypeAttachmentParsers;
    private final List<IndependentAttachmentParser<? extends Attachment>> independentAttachmentParsers;

    @Autowired
    public BlockchainLogicVerifier(List<GasCalculator> gasCalculatorList,
                                   List<TransactionTypeValidator> validators,
                                   List<TransactionTypeHandler> handlers,
                                   List<AttachmentTxTypeParser<? extends Attachment>> txTypeAttachmentParsers,
                                   List<IndependentAttachmentParser<? extends Attachment>> independentAttachmentParsers) {
        this.gasCalculatorList = gasCalculatorList;
        this.validators = validators;
        this.handlers = handlers;
        this.txTypeAttachmentParsers = txTypeAttachmentParsers;
        this.independentAttachmentParsers = independentAttachmentParsers;
    }

    @PostConstruct
    public void doValidate() {
        validate(gasCalculatorList, "Gas calculator");
        validate(validators, "Tx validator");
        validate(handlers, "Tx handler");
        validate(txTypeAttachmentParsers, "Tx attachment parser");
        validate(independentAttachmentParsers, "Independent Tx attachment parser", AttachmentTypedComponent::type, IndependentAttachmentType.values());
    }

    private <T> void validate(List<T> entities, String name, Function<T, Enum<?>> en, Enum<?>[] values) {

        Map<Enum<?>, List<T>> map = entities.stream().collect(Collectors.groupingBy(en));
        map.forEach((t, l) -> {
            if (l.size() > 1) {
                throw new RuntimeException(l.size() + " " + name + "s registered for type " + t + " :" + l);
            }
        });
        for (Enum<?> value : values) {
            if (map.get(value) == null) {
                throw new RuntimeException("No " + name + " defined for type " + value);
            }
        }
    }

    private <T extends TxTypedComponent> void validate(List<T> entities, String name) {
        validate(entities, name, T::type, TxType.values());
    }


    private <T extends TxTypedComponent> Map<TxType, T> getEntityMap(List<T> entities) {
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

    @Bean
    Map<TxType, AttachmentTxTypeParser<? extends Attachment>> txTypeParsers() {
        return getEntityMap(txTypeAttachmentParsers);
    }

    @Bean
    Map<IndependentAttachmentType, IndependentAttachmentParser<? extends Attachment>> independentAttachmentParsers() {
        return independentAttachmentParsers.stream().collect(Collectors.toMap(IndependentAttachmentParser::type, Function.identity()));
    }
}
