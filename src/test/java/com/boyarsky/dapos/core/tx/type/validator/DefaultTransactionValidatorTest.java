package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.validator.impl.DefaultTransactionValidator;
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
        validator = new DefaultTransactionValidator(service, null, null);
    }

    @Test
    void validate_correctTx() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 0, 100)
                .amount(100)
                .build(true);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        validator.validate(tx);
    }

    @Test
    void validate_noSenderAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 0, 100)
                .amount(100)
                .build(true);

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.SENDER_NOT_EXIST, ex.getCode());
    }

    @Test
    void validate_txWithPubKey_alreadyAssignedAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 0, 100).build(true);
        doReturn(new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.PUB_KEY_FOR_OLD_ACC, ex.getCode());
    }

    @Test
    void validate_txWithoutPubKey_newAccount() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 0, 100).build(false);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TxNotValidException exception = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.NO_PUB_KEY_FOR_NEW_ACC, exception.getCode());
    }

    @Test
    void validate_txWithIncorrectSignature_Format() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), null, new byte[0], 0, 0, 1, new byte[64]);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(ErrorCodes.WRONG_SIG_FORMAT, ex.getCode());
    }

    @Test
    void validate_txWithIncorrectSignature_another_data_signed() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        byte[] fakeSignature = CryptoUtils.sign(wallet.getKeyPair().getPrivate(), new byte[32]);
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), null, new byte[0], 0, 0, 20, fakeSignature);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(ErrorCodes.BAD_SIG, ex.getCode());
    }

    @Test
    void validate_txWithIncorrectPubKey() { //TODO replace by meaningful exception handling
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Wallet anotherWallet = CryptoUtils.generateEd25Wallet();
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(anotherWallet.getKeyPair().getPublic()), null, new byte[0], 20, 10, 200, null);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(invalidTx.getSender());

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(invalidTx));

        assertEquals(ErrorCodes.INCORRECT_PUB_KEY, ex.getCode());
    }

    @Test
    void validate_insufficient_balance() {
        Wallet wallet = CryptoUtils.generateEd25Wallet();
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 0, 2)
                .amount(100)
                .build(false);
        doReturn(new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), 99, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.NOT_ENOUGH_MONEY, ex.getCode());
    }

}