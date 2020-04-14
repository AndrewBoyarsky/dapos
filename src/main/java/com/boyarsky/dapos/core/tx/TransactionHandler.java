package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxHandlingException;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionHandler {
    private final Map<TxType, TransactionTypeHandler> handlers = new HashMap<>();

    @Autowired
    public TransactionHandler(List<TransactionTypeHandler> handlers) {
        Map<TxType, List<TransactionTypeHandler>> validatorMap = handlers.stream().collect(Collectors.groupingBy(TransactionTypeHandler::type));
        validatorMap.forEach((t, l) -> {
            if (l.size() > 1) {
                throw new RuntimeException(l.size() + " handlers registered for transaction of type " + t + " :" + l);
            }
            this.handlers.put(t, l.get(0));
        });
    }

    public void handleTx(Transaction tx) throws TxHandlingException {
        TransactionTypeHandler defaultHandler = handlers.get(TxType.ALL);
        if (defaultHandler != null) {
            defaultHandler.handle(tx);
        } else {
            throw new TxHandlingException("Default tx handler for all types is not defined", tx);
        }
        TransactionTypeHandler handler = handlers.get(tx.getType());
        if (handler == null) {
            throw new TxHandlingException("Handler for type " + tx.getType() + " was not found", tx);
        }
        handler.handle(tx);
    }
}
