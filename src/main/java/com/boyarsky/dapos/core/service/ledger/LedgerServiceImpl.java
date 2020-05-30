package com.boyarsky.dapos.core.service.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.ledger.LedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LedgerServiceImpl implements LedgerService {
    LedgerRepository repository;

    @Autowired
    public LedgerServiceImpl(LedgerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void add(LedgerRecord record) {
        repository.save(record);
    }

    @Override
    public List<LedgerRecord> records(AccountId id, Pagination pagination) {
        return repository.getRecords(id, pagination);
    }

    @Override
    public List<LedgerRecord> records(AccountId id, String type, Pagination pagination) {
        return repository.getRecords(id, type, pagination);
    }
}
