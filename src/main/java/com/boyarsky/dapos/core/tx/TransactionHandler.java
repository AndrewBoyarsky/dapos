package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TransactionHandler {
    private final Map<TxType, TransactionTypeHandler> handlers = new HashMap<>();

    @Autowired
    public TransactionHandler(Map<TxType, TransactionTypeHandler> handlers) {
        this.handlers.putAll(handlers);
    }

    public void handleTx(Transaction tx) throws TxHandlingException {
        TransactionTypeHandler defaultHandler = handlers.get(TxType.ALL);
        defaultHandler.handle(tx);
        TransactionTypeHandler handler = handlers.get(tx.getType());
        handler.handle(tx);
    }
}
