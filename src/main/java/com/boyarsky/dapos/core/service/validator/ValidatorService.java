package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;

import java.util.List;

public interface ValidatorService {
    ValidatorEntity registerValidator(byte[] pubKey, int fee, AccountId rewardAddress, boolean enable, long height);

    List<ValidatorEntity> getAllUpdated(long height);

    void toggleValidator(AccountId validatorId, boolean enabled, long height);

    void absentValidator(AccountId validatorId);

    long punishByzantine(AccountId validatorId, long height);

    void revoke(Transaction tx);

    long punishAbsent(AccountId validatorId, long height);

    void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount, long height);

    void addVote(Transaction tx, VoteAttachment attachment);

    ValidatorEntity get(AccountId id);
}

