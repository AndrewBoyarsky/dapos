package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;

import java.math.BigDecimal;

public interface StakeholderService {

    StakeholderPunishmentData punishStakeholders(AccountId validatorId, long height);

    StakeholderPunishmentData punishByzantineStakeholders(AccountId validatorId, long height);

    long voteFor(AccountId validatorId, AccountId voterId, long votePower, long height);

    long revokeVote(AccountId validatorId, AccountId voterId, long height);

    void distributeRewardForValidator(AccountId id, BigDecimal stakeholdersReward, long height);

    boolean exists(AccountId validator, AccountId voter);

    long minStake(AccountId validator);
}
