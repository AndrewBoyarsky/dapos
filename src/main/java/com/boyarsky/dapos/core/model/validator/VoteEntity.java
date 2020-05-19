package com.boyarsky.dapos.core.model.validator;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoteEntity extends BlockchainEntity {
    private AccountId accountId;
    private AccountId validatorId;
    private long totalStake;

}
