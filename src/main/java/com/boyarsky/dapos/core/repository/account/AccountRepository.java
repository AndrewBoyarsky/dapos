package com.boyarsky.dapos.core.repository.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;

import java.util.List;

public interface AccountRepository {
    Account find(AccountId accountId);

    void save(Account account); // update or insert

    List<Account> getAll();

    void delete(Account account);
}
