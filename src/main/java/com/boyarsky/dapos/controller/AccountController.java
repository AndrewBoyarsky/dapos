package com.boyarsky.dapos.controller;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/accounts")
public class AccountController {
    @Autowired
    private AccountService service;
    @GetMapping
    public List<Account> getAccounts() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable("id") String account) {
        return service.get(new AccountId(account));
    }
}
