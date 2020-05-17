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
public class FeeProvider extends BlockchainEntity {
    private long id;
    private AccountId account;
    private long balance;
    private State state;
    private PartyFeeConfig fromFeeConfig;
    private PartyFeeConfig toFeeConfig;

}
