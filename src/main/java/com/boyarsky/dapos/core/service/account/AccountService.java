package com.boyarsky.dapos.core.service.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;

import java.util.List;

public interface AccountService {

    Account get(AccountId accountId);

    boolean assignPublicKey(AccountId accountId, byte[] pubKey);

    List<Account> getAll();

    void transferMoney(AccountId sender, AccountId recipient, Operation op);

    void addToBalance(AccountId accountId, Operation op);

    void addToBalance(AccountId accountId, AccountId senderId, Operation op);

    void save(Account account);

}
