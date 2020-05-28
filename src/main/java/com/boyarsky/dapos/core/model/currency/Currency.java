package com.boyarsky.dapos.core.model.currency;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency extends BlockchainEntity {
    private long currencyId;
    private String code;
    private String name;
    private String description;
    private AccountId issuer;
    private long supply;
    private long reserve;
    private byte decimals;

    public Currency(long height, long currencyId, String code, String name, String description, AccountId issuer, long supply, long reserve, byte decimals) {
        super(height);
        this.code = code;
        this.currencyId = currencyId;
        this.name = name;
        this.description = description;
        this.issuer = issuer;
        this.supply = supply;
        this.reserve = reserve;
        this.decimals = decimals;
    }
}
