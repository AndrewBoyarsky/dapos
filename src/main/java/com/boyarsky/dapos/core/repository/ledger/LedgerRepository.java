package com.boyarsky.dapos.core.repository.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.tx.type.TxType;

import java.util.List;

public interface LedgerRepository {

    List<LedgerRecord> getRecords(AccountId id, TxType type);

    List<LedgerRecord> getRecords(AccountId id);

    void save(LedgerRecord record);
}
