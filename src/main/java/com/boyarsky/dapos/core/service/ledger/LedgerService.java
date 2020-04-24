package com.boyarsky.dapos.core.service.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.tx.type.TxType;

import java.util.List;

public interface LedgerService {
    void add(LedgerRecord record);

    List<LedgerRecord> records(AccountId id);

    List<LedgerRecord> records(AccountId id, TxType type);
}
