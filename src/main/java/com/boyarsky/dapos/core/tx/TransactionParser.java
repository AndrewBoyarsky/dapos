package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TransactionParser {
    public Transaction parseTx(byte[] txBytes) {
        Transaction transaction = new Transaction(txBytes);
        return transaction;
    }
}
