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
        Account account = service.get(tx.getSender());
        if (account == null) {
            throw new TxNotValidException("Sender account does not exist: " + tx.getSender().toString(), tx, -11);
        }
        if (account.getPublicKey() == null && !tx.isFirst()) {
            throw new TxNotValidException("Sender's account public key is not exist, required sender public key in transaction body", tx, -12);
        }
        if (account.getPublicKey() != null && tx.isFirst()) {
            throw new TxNotValidException("Tx must not contain sender's public key when sender's account already has assigned key", tx, -13);
        }
        PublicKey verifKey;
        try {
            if (tx.isFirst()) {
                verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), tx.getSenderPublicKey());
            } else {
                verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), account.getPublicKey());
            }
        } catch (InvalidKeyException e) {
            throw new TxNotValidException("Incorrect public key provided", tx, -14, e);
        }
        boolean verified;
        try {
            byte[] sig = tx.getSignature();
            if (!tx.isEd()) {
                sig = CryptoUtils.uncompressSignature(sig);
            }
            byte[] signableBytes = tx.bytes(true);
            verified = CryptoUtils.verifySignature(tx.isEd(), sig, verifKey, signableBytes);
        } catch (InvalidKeyException e) { // should never happens
            throw new TxNotValidException("FATAL ERROR! Inappropriate public key provided for signature verification", tx, -15, e);
        } catch (SignatureException e) {
            throw new TxNotValidException("Invalid signature format provided", tx, -16, e);
        }
        if (!verified) {
            throw new TxNotValidException("Incorrect signature", tx, -17);
        }

        long balance = account.getBalance();
        if (balance < tx.getAmount()) {
            throw new TxNotValidException("Not sufficient funds, got " + balance + ", expected " + tx.getAmount(), tx, -18);
        }
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }
}
