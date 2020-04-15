package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TypedComponent;

public interface TransactionTypeValidator extends TypedComponent {

    void validate(Transaction tx) throws TxNotValidException;

}
