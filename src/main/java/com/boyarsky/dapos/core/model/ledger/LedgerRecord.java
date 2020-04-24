package com.boyarsky.dapos.core.model.ledger;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.TxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerRecord extends BlockchainEntity {
    private long id;
    private long fee;
    private long amount;
    private TxType type;
    private AccountId sender;
    private AccountId recipient;
}
