package com.boyarsky.dapos.core.model.validator;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ValidatorEntity extends BlockchainEntity {
    private boolean enabled;
    private byte[] publicKey;
    private long absentFor;
    private AccountId id;
    private AccountId rewardId;
    private int votes;
    //    private long balance;
    private long votePower;
    private int fee;
}
