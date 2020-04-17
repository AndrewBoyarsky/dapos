package com.boyarsky.dapos.core.service.account;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;

import java.util.List;

public interface AccountService {

    Account get(AccountId accountId);

    boolean assignPublicKey(AccountId accountId, byte[] pubKey);

    List<Account> getAll();

    void transferMoney(AccountId sender, AccountId recipient, long amount);


    void save(Account account);

}
