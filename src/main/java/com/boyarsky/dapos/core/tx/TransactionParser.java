package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.Transaction;

public class TransactionParser {
    public Transaction parseTx(byte[] txBytes) {
        Transaction transaction = new Transaction(txBytes);
        return transaction;
    }
}
