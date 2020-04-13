package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.core.account.Wallet;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionValidatorTest {
    @Mock
    AccountService service;
    DefaultTransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefaultTransactionValidator(service);
    }

    @Test
    void validate_correctTx() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, wallet.getAccount(), wallet.getKeyPair(), 0)
                .amount(100)
                .build(true);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        validator.validate(tx);
    }

    @Test
    void validate_noSenderAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, wallet.getAccount(), wallet.getKeyPair(), 0)
                .amount(100)
                .build(true);

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(-11, ex.getCode());
    }

    @Test
    void validate_txWithPubKey_alreadyAssignedAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, wallet.getAccount(), wallet.getKeyPair(), 0).build(true);
        doReturn(new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(-13, ex.getCode());
    }

    @Test
    void validate_txWithoutPubKey_newAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, wallet.getAccount(), wallet.getKeyPair(), 0).build(false);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TransactionTypeValidator.TxNotValidException exception = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(-12, exception.getCode());
    }

    @Test
    void validate_txWithIncorrectSignature_Format() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), null, new byte[0], 0, 0, new byte[64]);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(-16, ex.getCode());
    }

    @Test
    void validate_txWithIncorrectSignature_another_data_signed() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        byte[] fakeSignature = CryptoUtils.sign(wallet.getKeyPair().getPrivate(), new byte[32]);
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), null, new byte[0], 0, 0, fakeSignature);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(-17, ex.getCode());
    }

    @Test
    void validate_txWithIncorrectPubKey() { //TODO replace by meaningful exception handling
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Wallet anotherWallet = CryptoUtils.generateEd25Wallet();
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(anotherWallet.getKeyPair().getPublic()), null, new byte[0], 20, 10, null);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(-14, ex.getCode());
    }

    @Test
    void validate_insufficient_balance() {
        Wallet wallet = CryptoUtils.generateEd25Wallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, wallet.getAccount(), wallet.getKeyPair(), 0)
                .amount(100)
                .build(false);
        doReturn(new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), 99, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TransactionTypeValidator.TxNotValidException ex = assertThrows(TransactionTypeValidator.TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(-18, ex.getCode());
    }

}