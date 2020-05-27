package com.boyarsky.dapos.core.model.currency;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Currency extends BlockchainEntity {
    private long currencyId;
    private String code;
    private String name;
    private String description;
    private AccountId issuer;
    private long supply;
    private int reservePerUnit;
    private byte decimals;

    public Currency(long height, long currencyId, String code, String name, String description, AccountId issuer, long supply, int reservePerUnit, byte decimals) {
        super(height);
        this.code = code;
        this.currencyId = currencyId;
        this.name = name;
        this.description = description;
        this.issuer = issuer;
        this.supply = supply;
        this.reservePerUnit = reservePerUnit;
        this.decimals = decimals;
    }
}
