package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;

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
        if (account == null && !tx.isFirst() ) {
            throw new TxNotValidException("Sender account is not exist, required sender public key in transaction body", tx, -11);
        }
        if (account != null && tx.isFirst()) {
            throw new TxNotValidException("Tx must not contain sender's public key when sender's account already has assigned key", tx, -12);
        }
        PublicKey verifKey;
        if (tx.isFirst()) {
            verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), tx.getSenderPublicKey());
        } else {
            verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), account.getPublicKey());
        }
        boolean verified;
        try {
            verified = CryptoUtils.verifySignature(tx.getSignature(), verifKey, bytesToVerify);
        } catch (InvalidKeyException e) {
            throw new TxNotValidException("Incorrect public key", tx, -13, e);
        } catch (SignatureException e) {
            throw new TxNotValidException("Invalid signature format provided", tx, -14, e);
        }
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
