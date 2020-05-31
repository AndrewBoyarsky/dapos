package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface VoterService {

    StakeholderPunishmentData punishStakeholders(AccountId validatorId, long height);

    StakeholderPunishmentData punishByzantineStakeholders(AccountId validatorId, long height);

    long voteFor(AccountId validatorId, AccountId voterId, long votePower, long height);

    long revokeVote(AccountId validatorId, AccountId voterId, long height);

    void distributeRewardForValidator(AccountId id, long stakeholdersReward, long height);

    boolean exists(AccountId validator, AccountId voter);

    VoteEntity get(AccountId validator, AccountId voter);

    List<VoteEntity> getByVoter(AccountId voter, Pagination pagination);

    List<VoteEntity> getAll(Pagination pagination);

    List<VoteEntity> getValidatorVoters(AccountId validator, Pagination pagination);

    long minStake(AccountId validator);
}
