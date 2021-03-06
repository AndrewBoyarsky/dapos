package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.service.validator.VoterService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteTransactionValidator implements TransactionTypeValidator {
    private ValidatorService service;
    private VoterService voterService;
    private AccountService accountService;
    private BlockchainConfig config;

    @Autowired
    public VoteTransactionValidator(ValidatorService service, VoterService voterService,
                                    AccountService accountService, BlockchainConfig config) {
        this.service = service;
        this.voterService = voterService;
        this.accountService = accountService;
        this.config = config;
    }

    @Override
    public void validate(Transaction tx) {
        if (tx.getRecipient() == null) {
            throw new TxNotValidException("Validator address should be set as recipient for vote tx, got 'null'", null, tx, ErrorCodes.VOTE_VALIDATOR_NOT_SPECIFIED);
        }
        ValidatorEntity validator = service.get(tx.getRecipient());
        if (validator == null) {
            throw new TxNotValidException("Validator for address " + tx.getRecipient() + " was not found", null, tx, ErrorCodes.VOTE_VALIDATOR_NOT_FOUND);
        }
        if (!validator.isEnabled()) {
            throw new TxNotValidException("Validator " + validator.getId() + " is disabled", null, tx, ErrorCodes.VOTE_VALIDATOR_DISABLED);
        }
        if (tx.getAmount() < config.getMinVoteStake()) {
            throw new TxNotValidException("Vote power is less than minimal limit, required >= " + config.getMinVoteStake() + ", got " + tx.getAmount(), null, tx, ErrorCodes.VOTE_POWER_LESSER_THAN_MINIMAL_STAKE);
        }
        if (!voterService.exists(tx.getRecipient(), tx.getSender())) {
            if (config.getMaxValidatorVotes() == validator.getVotes()) {
                long minStake = voterService.minStake(validator.getId());
                if (tx.getAmount() <= minStake) {
                    throw new TxNotValidException("Vote power is not enough to supersede the weakest validator's voter, required more than " + minStake + ", got " + tx.getAmount(), null, tx, ErrorCodes.VOTE_FAILED_TO_SUPERSEDE);
                }
            }
        }
    }

    @Override
    public TxType type() {
        return TxType.VOTE;
    }
}
