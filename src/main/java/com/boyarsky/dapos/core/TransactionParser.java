package com.boyarsky.dapos.core;

import com.boyarsky.dapos.core.tx.Transaction;

public class TransactionParser {
    public Transaction parseTx(String rawTransaction) {
        Transaction transaction = new Transaction(rawTransaction);
        return transaction;
    }
}
