package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;

import java.math.BigDecimal;

public interface StakeholderService {

    long punishStakeholders(AccountId validatorId, long height);

    long punishByzantineStakeholders(AccountId validatorId, long height);

    void voteFor(Transaction tx, VoteAttachment attachment);

    void revokeVote(Transaction tx, VoteAttachment attachment);

    void distributeRewardForValidator(AccountId id, BigDecimal stakeholdersReward);
}
