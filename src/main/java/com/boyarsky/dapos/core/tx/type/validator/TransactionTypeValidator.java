package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxTypedComponent;

public interface TransactionTypeValidator extends TxTypedComponent {

    void validate(Transaction tx);

}
