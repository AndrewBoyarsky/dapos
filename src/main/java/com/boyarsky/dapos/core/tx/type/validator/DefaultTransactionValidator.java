package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransactionValidator implements TransactionTypeValidator{
    private AccountService service;

    @Autowired
    public DefaultTransactionValidator(AccountService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        byte[] bytesToVerify = tx.bytes(true);
        Account account = service.get(tx.getSender());
        boolean verified = CryptoUtils.verifySignature(tx.getSignature(), account.getPublicKey(), bytesToVerify);
        if (!verified) {
            throw new TxNotValidException("Incorrect signature", tx, -11);
        }
        long balance = account.getBalance();
        if (balance < tx.getAmount()) {
            throw new TxNotValidException("Not sufficient funds, got " + balance + ", expected " + tx.getAmount(), tx, -12);
        }
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }
}
