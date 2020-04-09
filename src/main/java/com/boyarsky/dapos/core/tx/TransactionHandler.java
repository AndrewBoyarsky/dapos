package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionHandler {
    private final Map<TxType, TransactionTypeHandler> handlers = new HashMap<>();
    private AccountService service;

    @Autowired
    public TransactionHandler(AccountService service, List<TransactionTypeHandler> handlers) {
        this.service = service;
        Map<TxType, List<TransactionTypeHandler>> validatorMap = handlers.stream().collect(Collectors.groupingBy(TransactionTypeHandler::type));
        validatorMap.forEach((t, l) -> {
            if (l.size() > 1) {
                throw new RuntimeException(l.size() + " handlers registered for transaction of type " + t + " :" + l);
            }
            this.handlers.put(t, l.get(0));
        });
    }

    public void handleTx(Transaction tx) {
        TransactionTypeHandler handler = handlers.get(tx.getType());
        if (handler == null) {
            throw new RuntimeException("Handler for type " + tx.getType() + " was not found");
        }
        handler.handle(tx);
    }

}
