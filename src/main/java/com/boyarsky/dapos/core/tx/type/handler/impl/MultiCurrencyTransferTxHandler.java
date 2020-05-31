package com.boyarsky.dapos.core.tx.type.handler.impl;

import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyMultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.handler.TransactionTypeHandler;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiCurrencyTransferTxHandler implements TransactionTypeHandler {
    private CurrencyService currencyService;

    @Autowired
    public MultiCurrencyTransferTxHandler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void handle(Transaction tx) throws TxHandlingException {
        currencyService.multiTransfer(tx, tx.getAttachment(CurrencyMultiAccountAttachment.class));
    }

    @Override
    public TxType type() {
        return TxType.MULTI_CURRENCY_TRANSFER;
    }
}
