package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;

import java.math.BigDecimal;

public interface StakeholderService {

    StakeholderPunishmentData punishStakeholders(AccountId validatorId, long height);

    StakeholderPunishmentData punishByzantineStakeholders(AccountId validatorId, long height);

    long voteFor(Transaction tx, VoteAttachment attachment);

    long revokeVote(AccountId validator, AccountId voter, long height);

    void distributeRewardForValidator(AccountId id, BigDecimal stakeholdersReward);

    boolean exists(AccountId validator, AccountId voter);

    long minStake(AccountId validator);
}
