package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.env.Transaction;

import java.util.List;

public interface AccountRepository {
    Account find(AccountId accountId);

    void save(Account account); // update or insert

    List<Account> getAll();

    void delete(Account account);
}
