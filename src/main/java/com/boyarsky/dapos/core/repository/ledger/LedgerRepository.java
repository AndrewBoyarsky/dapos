package com.boyarsky.dapos.core.repository.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface LedgerRepository {


    List<LedgerRecord> getRecords(AccountId id, Pagination pagination);

    void save(LedgerRecord record);

    List<LedgerRecord> getRecords(AccountId id, String type, Pagination pagination);
}
