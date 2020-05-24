package com.boyarsky.dapos.core.model.validator;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class VoteEntity extends BlockchainEntity {
    private AccountId accountId;
    private AccountId validatorId;
    private long totalPower;

    public VoteEntity(long height, AccountId accountId, AccountId validatorId, long totalPower) {
        super(height);
        this.accountId = accountId;
        this.validatorId = validatorId;
        this.totalPower = totalPower;
    }
}
