package com.boyarsky.dapos.core;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesisImpl implements Genesis {
    @Autowired
    private AccountService accountService;
    @Override
    public void initialize() {
//        accountService.save(new Account(AccountId.fromBytes(), new byte[32],1000, Account.Type.ORDINARY));
//        accountService.save(new Account(AccountId.fromBytes(), new byte[32],1000, Account.Type.ORDINARY));
//        accountService.save(new Account(AccountId.fromBytes(), new byte[32],1000, Account.Type.ORDINARY));
//        accountService.save(new Account(AccountId.fromBytes(), new byte[32],1000, Account.Type.ORDINARY));
    }
}
