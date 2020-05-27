package com.boyarsky.dapos.core.model.currency;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CurrencyAccount extends BlockchainEntity {
    private AccountId holder;
    private long currencyId;
    private long amount;


    public CurrencyAccount(long height, AccountId holder, long currencyId, long amount) {
        super(height);
        this.holder = holder;
        this.currencyId = currencyId;
        this.amount = amount;
    }
}
