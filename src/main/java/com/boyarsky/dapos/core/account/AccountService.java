package com.boyarsky.dapos.core.account;

import java.util.List;

public interface AccountService {

    Account get(AccountId accountId);

    boolean assignPublicKey(AccountId accountId, byte[] pubKey);

    List<Account> getAll();

    void transferMoney(AccountId sender, AccountId recipient, long amount);


    void save(Account account);

}
