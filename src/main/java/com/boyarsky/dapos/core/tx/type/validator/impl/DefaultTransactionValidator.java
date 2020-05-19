package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.AccountFeeAllowance;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultTransactionValidator implements TransactionTypeValidator {
    private final AccountService service;
    private final FeeProviderService feeProviderService;
    private final MessageTransactionValidator validator;

    @Autowired
    public DefaultTransactionValidator(AccountService service, FeeProviderService feeProviderService, MessageTransactionValidator validator) {
        this.service = service;
        this.feeProviderService = feeProviderService;
        this.validator = validator;
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
                try {
                    sig = CryptoUtils.uncompressSignature(sig);
                } catch (RuntimeException e) {
                    throw new TxNotValidException("Invalid signature content", e, tx, ErrorCodes.WRONG_SIG_FORMAT);
                }
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
        long totalChargeAmount = tx.getAmount();
        NoFeeAttachment noFeeAttachment = tx.getAttachment(NoFeeAttachment.class);
        if (noFeeAttachment != null) {
            FeeProvider feeProvider = feeProviderService.get(noFeeAttachment.getPayer());
            if (feeProvider == null) {
                throw new TxNotValidException("Fee provider does not exist with id: " + noFeeAttachment.getPayer(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_EXIST);
            }
            if (feeProvider.getState() != State.ACTIVE) {
                throw new TxNotValidException("Fee provider is not active, id " + feeProvider.getId() + ", current state - " + feeProvider.getState(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_ENABLED);
            }
            if (feeProvider.getBalance() < tx.getMaxFee()) {
                throw new TxNotValidException("Expected fee " + tx.getMaxFee() + ", but can be payed only " + feeProvider.getBalance() + " for provider " + feeProvider.getId(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_FUNDS);
            }
            PartyFeeConfig fromFeeConfig = feeProvider.getFromFeeConfig();
            validateForParty(tx, fromFeeConfig, tx.getSender(), feeProvider);
            PartyFeeConfig toFeeConfig = feeProvider.getToFeeConfig();

            if (tx.getRecipient() != null) {
                validateForParty(tx, toFeeConfig, tx.getRecipient(), feeProvider);
            }
        } else {
            totalChargeAmount += tx.getMaxFee();
        }
        long balance = account.getBalance();
        if (balance < totalChargeAmount) {
            throw new TxNotValidException("Not sufficient funds, got " + balance + ", expected " + totalChargeAmount, null, tx, ErrorCodes.NOT_ENOUGH_MONEY);
        }
        MessageAttachment messageAttachment = tx.getAttachment(MessageAttachment.class);
        if (messageAttachment != null) {
            validator.validate(tx);
        }

    }

    private void validateForParty(Transaction tx, PartyFeeConfig partyFeeConfig, AccountId accountId, FeeProvider feeProvider) throws TxNotValidException {
        if (partyFeeConfig.isWhitelistAll()) {
            FeeConfig rootConfig = partyFeeConfig.getRootConfig();
            if (rootConfig != null) {
                validateForConfig(tx, rootConfig, feeProvider, accountId);
            }
        } else {
            Map<FeeConfig, List<AccountId>> configs = partyFeeConfig.getConfigs();
            Optional<FeeConfig> feeConfigOpt = configs.entrySet().stream().filter(e -> e.getValue().contains(accountId)).map(Map.Entry::getKey).findAny();
            if (feeConfigOpt.isEmpty()) {
                throw new TxNotValidException("Account " + accountId + " is not whitelisted for fee provider " + feeProvider.getId(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_WHITELISTED_SENDER);
            }
            FeeConfig feeConfig = feeConfigOpt.get();
            validateForConfig(tx, feeConfig, feeProvider, accountId);
        }
    }

    private void validateForConfig(Transaction tx, FeeConfig config, FeeProvider feeProvider, AccountId accountId) throws TxNotValidException {
        if (!config.allowed(tx.getType())) {
            throw new TxNotValidException("Transaction of type " + tx.getType() + " is not allowed for provider " + feeProvider.getId() + ", expected - " + config.getAllowedTxs(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_SUPPORTED_TX_TYPE);
        }
        if (config.getMaxAllowedFee() != -1 && config.getMaxAllowedFee() < tx.getMaxFee()) {
            throw new TxNotValidException("Transaction require more fee to succeed than available. Require: " + tx.getMaxFee() + ", available " + config.getMaxAllowedFee(), null, tx, ErrorCodes.FEE_PROVIDER_EXCEED_LIMIT_PER_OP);
        }
        if (config.getMaxAllowedTotalFee() != -1 && config.getMaxAllowedTotalFee() < tx.getMaxFee()) {
            throw new TxNotValidException("Transaction exceed max total fee limit. Require " + tx.getMaxFee() + ", total available for account " + config.getMaxAllowedTotalFee(), null, tx, ErrorCodes.FEE_PROVIDER_EXCEED_TOTAL_LIMIT);
        }
        AccountFeeAllowance allowance = feeProviderService.allowance(feeProvider.getId(), accountId);
        if (allowance != null) {
            if (allowance.getOperations() == 0) {
                throw new TxNotValidException("No operations available for account " + accountId + " on fee provider " + feeProvider.getId(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_AVAILABLE_OPS);
            }
            if (allowance.getFeeRemaining() < tx.getMaxFee()) {
                throw new TxNotValidException("Not enough fee remains to use for provider: " + feeProvider.getId() + ", required " + tx.getMaxFee() + ", remaining " + allowance.getFeeRemaining(), null, tx, ErrorCodes.FEE_PROVIDER_NOT_ENOUGH_AVAIlABLE_FEE_LIMIT);
            }
        }
    }

    @Override
    public TxType type() {
        return TxType.ALL;
    }
}
