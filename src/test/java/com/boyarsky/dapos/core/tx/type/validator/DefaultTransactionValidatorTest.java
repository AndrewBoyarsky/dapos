package com.boyarsky.dapos.core.tx.type.validator;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.validator.impl.DefaultTransactionValidator;
import com.boyarsky.dapos.core.tx.type.validator.impl.MessageTransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionValidatorTest {
    @Mock
    AccountService service;
    @Mock
    FeeProviderService feeProviderService;
    @Mock
    MessageTransactionValidator messageValidator;

    DefaultTransactionValidator validator;
    AccountId accountId1 = new AccountId("1P8LnS8QAVe23GGfdoy9XBU9hGacDaS1xe");
    AccountId accountId2 = new AccountId("1P8LnS8QAVe23GGfdoy9XBU9hGacDaS1xa");
    AccountId recipient = new AccountId("0xd3ef7139bdea050bd26543294aad956c1333a723");
    Wallet wallet = CryptoUtils.generateEd25Wallet();

    @BeforeEach
    void setUp() {
        validator = new DefaultTransactionValidator(service, feeProviderService, messageValidator);
    }

    @Test
    void validate_correctTx() throws TxNotValidException {
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
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(),
                wallet.getKeyPair(), 0, 100).build(false);
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        TxNotValidException exception = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.NO_PUB_KEY_FOR_NEW_ACC, exception.getCode());
    }

    @Test
    void validate_txWithIncorrectSignature_Format() {
        Wallet wallet = CryptoUtils.generateBitcoinWallet();
        Transaction invalidTx = new Transaction((byte) 1, TxType.PAYMENT, wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()),
                null, new byte[0], 0, 0, 1, new byte[64]);
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

    @Test
    void validate_feeProvider() {
//        not exist
        Transaction tx = createFeeProvTx();

        TxNotValidException ex = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_EXIST, ex.getCode());
//      not active
        FeeProvider feeProv = new FeeProvider(3993, accountId1, 100L, State.SUSPENDED, new PartyFeeConfig(true, null, null), new PartyFeeConfig(true, null, null));
        doReturn(feeProv).when(feeProviderService).get(3993);

        TxNotValidException eNotActive = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_ENABLED, eNotActive.getCode());
//       not enough to cover fees
        feeProv.setBalance(1199);
        feeProv.setState(State.ACTIVE);

        TxNotValidException eNotEnoughBalance = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_FUNDS, eNotEnoughBalance.getCode());

        verifyForParty(true, tx, feeProv, feeProv.getFromFeeConfig(), tx.getSender());
        verifyForParty(false, tx, feeProv, feeProv.getToFeeConfig(), tx.getRecipient());
    }

    private void verifyForParty(boolean sender, Transaction tx, FeeProvider feeProv, PartyFeeConfig config, AccountId accountId) {
//        tx type not allowed
        feeProv.setBalance(1200);
        FeeConfig rootConfig = new FeeConfig(-1, -1, -1, Set.of());
        config.setRootConfig(rootConfig);
        rootConfig.setAllowedTxs(Set.of(TxType.MESSAGE, TxType.REVOKE));

        TxNotValidException notAllowedTxEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_SUPPORTED_TX_TYPE, notAllowedTxEx.getCode());

//      max allowed fee is less than tx require
        rootConfig.setAllowedTxs(Set.of()); // allow all tx types
        rootConfig.setMaxAllowedFee(1199);

        TxNotValidException maxAllowedFeeExceeded = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_EXCEED_LIMIT_PER_OP, maxAllowedFeeExceeded.getCode());

//      tx fee exceed total limit for account
        rootConfig.setMaxAllowedFee(-1);
        rootConfig.setMaxAllowedTotalFee(1199);

        TxNotValidException maxTotalAllowedFeeEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_EXCEED_TOTAL_LIMIT, maxTotalAllowedFeeEx.getCode());

//      zero ops left for account
        rootConfig.setMaxAllowedTotalFee(-1);
        AccountFeeAllowance allowance = new AccountFeeAllowance(accountId, 3993, sender, 0, Long.MAX_VALUE);
        doReturn(allowance).when(feeProviderService).allowance(3993L, accountId, sender);

        TxNotValidException noOpsEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_AVAILABLE_OPS, noOpsEx.getCode());

//       not enough amount left to cover fees for the account
        allowance.setOperations(1);
        allowance.setFeeRemaining(1199);

        TxNotValidException maxAllowedFeeForAllowanceExceeded = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_AVAIlABLE_FEE_LIMIT, maxAllowedFeeForAllowanceExceeded.getCode());
//       not whitelisted acc
        config.setWhitelistAll(false);
        FeeConfig newFeeConfig = new FeeConfig(1200, 2, 300, Set.of());
        FeeConfig stubFeeConfig = new FeeConfig(30000, 5, 3600, Set.of());
        List<AccountId> firstAccounts = new ArrayList<>(List.of(accountId1));
        config.setConfigs(Map.of(
                newFeeConfig, firstAccounts,
                stubFeeConfig, List.of(accountId2)
        ));
        TxNotValidException noConfigForAcc = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_WHITELISTED_SENDER, noConfigForAcc.getCode());
//        not allowed tx type
        newFeeConfig.setAllowedTxs(Set.of(TxType.MESSAGE));
        firstAccounts.add(accountId);

        TxNotValidException incorrectTypeTx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.FEE_PROVIDER_NOT_SUPPORTED_TX_TYPE, incorrectTypeTx.getCode());
        //        ok
        newFeeConfig.setAllowedTxs(Set.of());
        newFeeConfig.setMaxAllowedFee(1200);
        newFeeConfig.setMaxAllowedTotalFee(1200);
        allowance.setFeeRemaining(1200);
        validator.validate(tx);
    }

    Transaction createFeeProvTx() {
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 3, 400)
                .amount(100)
                .recipient(recipient)
                .build(true);
        tx.putAttachment(new PaymentAttachment());
        tx.putAttachment(new NoFeeAttachment((byte) -1, 3993));
        doReturn(new Account(wallet.getAccount(), null, 100, Account.Type.ORDINARY)).when(service).get(tx.getSender());
        return tx;
    }

    @Test
    void testMessageValidate() {
        Transaction tx = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), wallet.getAccount(), wallet.getKeyPair(), 10, 1000)
                .amount(100)
                .recipient(recipient)
                .build(true);
        tx.putAttachment(new PaymentAttachment());
        tx.putAttachment(new MessageAttachment((byte) -1, new EncryptedData(TestUtil.generateBytes(32), TestUtil.generateBytes(32)), true, true));
        doReturn(new Account(wallet.getAccount(), null, 100000, Account.Type.ORDINARY)).when(service).get(tx.getSender());

        validator.validate(tx);

        verify(messageValidator).validate(tx);
    }


}