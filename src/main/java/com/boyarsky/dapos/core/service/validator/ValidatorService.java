package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;

import java.util.List;

public interface ValidatorService {
    ValidatorEntity registerValidator(byte[] pubKey, int fee, AccountId rewardAddress, boolean enable, long height);

    List<ValidatorEntity> getAllUpdated(long height);

    void toggleValidator(AccountId validatorId, boolean enabled, long height);

    long punishByzantine(AccountId validatorId, long height);

    void revoke(AccountId validatorId, AccountId voterId, long height);

    long punishAbsent(AccountId validatorId, long height);

    void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount, long height);

    void addVote(AccountId validatorId, AccountId voterId, long voterPower, long height);

    ValidatorEntity get(AccountId id);
}

