package com.boyarsky.dapos.core.model.validator;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ValidatorEntity extends BlockchainEntity {
    private boolean enabled;
    private byte[] publicKey;
    private long absentFor;
    private AccountId id;
    //    private long balance;
    private long delegatedBalance;
    private long fee;
}
