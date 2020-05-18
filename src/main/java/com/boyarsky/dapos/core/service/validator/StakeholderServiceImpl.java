package com.boyarsky.dapos.core.service.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;

import java.math.BigDecimal;

public class StakeholderServiceImpl implements StakeholderService {

    @Override
    public long punishStakeholders(AccountId validatorId, long height) {
        return 0;
    }

    @Override
    public long punishByzantineStakeholders(AccountId validatorId, long height) {
        return 0;
    }

    @Override
    public void voteFor(Transaction tx, VoteAttachment attachment) {

    }

    @Override
    public void revokeVote(Transaction tx, VoteAttachment attachment) {

    }

    @Override
    public void distributeRewardForValidator(AccountId id, BigDecimal stakeholdersReward) {

    }
}
