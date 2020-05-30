package com.boyarsky.dapos.core.service.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface LedgerService {
    void add(LedgerRecord record);

    List<LedgerRecord> records(AccountId id, Pagination pagination);

    List<LedgerRecord> records(AccountId id, String type, Pagination pagination);
}
