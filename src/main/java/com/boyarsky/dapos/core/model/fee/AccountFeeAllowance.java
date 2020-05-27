package com.boyarsky.dapos.core.model.fee;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountFeeAllowance extends BlockchainEntity {
    private AccountId account;
    private long provId;
    private int operations;
    private long feeRemaining;

    public AccountFeeAllowance(long height, AccountId account, long provId, int operations, long feeRemaining) {
        super(height);
        this.account = account;
        this.provId = provId;
        this.operations = operations;
        this.feeRemaining = feeRemaining;
    }
}
