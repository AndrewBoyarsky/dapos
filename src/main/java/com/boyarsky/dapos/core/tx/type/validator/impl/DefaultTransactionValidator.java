package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;

@Component
public class DefaultTransactionValidator implements TransactionTypeValidator {
    private AccountService service;

    @Autowired
    public DefaultTransactionValidator(AccountService service) {
        this.service = service;
    }

    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getType() == TxType.ALL) {
            throw new TxNotValidException("Tx should be of concreate type, not 'ALL'", null, tx, ErrorCodes.UNDEFINED_TYPE);
        }
        Account account = service.get(tx.getSender());
        if (account == null) {
            throw new TxNotValidException("Sender account does not exist: " + tx.getSender().toString(), null, tx, ErrorCodes.SENDER_NOT_EXIST);
        }
        if (account.getPublicKey() == null && !tx.isFirst()) {
            throw new TxNotValidException("Sender's account public key is not exist, required sender public key in transaction body", null, tx, ErrorCodes.NO_PUB_KEY_FOR_NEW_ACC);
        }
        if (account.getPublicKey() != null && tx.isFirst()) {
            throw new TxNotValidException("Tx must not contain sender's public key when sender's account already has assigned key", null, tx, ErrorCodes.PUB_KEY_FOR_OLD_ACC);
        }
        PublicKey verifKey;
        try {
            if (tx.isFirst()) {
                verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), tx.getSenderPublicKey());
            } else {
                verifKey = CryptoUtils.getUncompressedPublicKey(tx.isEd(), account.getPublicKey());
            }
        } catch (InvalidKeyException e) {
            throw new TxNotValidException("Incorrect public key provided", e, tx, ErrorCodes.INCORRECT_PUB_KEY);
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
            throw new TxNotValidException("FATAL ERROR! Inappropriate public key provided for signature verification", e, tx, ErrorCodes.FATAL_INCORRECT_PUB_KEY);
        } catch (SignatureException e) {
            throw new TxNotValidException("Invalid signature format provided", e, tx, ErrorCodes.WRONG_SIG_FORMAT);
        }
        if (!verified) {
            throw new TxNotValidException("Incorrect signature", null, tx, ErrorCodes.BAD_SIG);
        }

        long balance = account.getBalance();
        if (balance < tx.getAmount()) {
            throw new TxNotValidException("Not sufficient funds, got " + balance + ", expected " + tx.getAmount(), null, tx, ErrorCodes.NOT_ENOUGH_MONEY);
        }
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }
}
