package com.boyarsky.dapos.core.account;

import java.util.List;

public interface AccountService {

    Account get(AccountId accountId);

    List<Account> getAll();

    void transferMoney(AccountId sender, AccountId recipient, long amount);

    void save(Account account);

}
