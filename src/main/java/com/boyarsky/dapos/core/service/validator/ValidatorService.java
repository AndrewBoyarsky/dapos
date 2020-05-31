package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;
import java.util.Set;

public interface ValidatorService {
    ValidatorEntity registerValidator(byte[] pubKey, int fee, AccountId rewardAddress, boolean enable, long height);

    List<ValidatorEntity> getAll(long height);

    List<ValidatorEntity> getAll(Boolean enabled, Pagination pagination);

    void toggleValidator(AccountId validatorId, boolean enabled, long height);

    long punishByzantine(AccountId validatorId, long height);

    long punishByzantines(Set<AccountId> validators, long height);

    void revoke(AccountId validatorId, AccountId voterId, long height);

    long punishAbsent(AccountId validatorId, long height);

    long punishAbsents(Set<AccountId> validators, long height);

//    void distributeReward(List<ValidatorEntity> fairValidators, long rewardAmount, long height);

    void distributeReward(Set<AccountId> validators, long rewardAmount, long height);

    void addVote(AccountId validatorId, AccountId voterId, long voterPower, long height);

    ValidatorEntity get(AccountId id);
}

