package com.boyarsky.dapos.core.model;

import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeProvider {
    private long id;
    private AccountId account;
    private long balance;
    private State state;
    private PartyFeeConfig fromFeeConfig;
    private PartyFeeConfig toFeeConfig;

}
