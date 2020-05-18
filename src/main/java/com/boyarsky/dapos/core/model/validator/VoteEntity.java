package com.boyarsky.dapos.core.model.validator;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;

public class VoteEntity extends BlockchainEntity {
    private AccountId accountId;
    private AccountId validatorId;
    private long totalStake;

}
