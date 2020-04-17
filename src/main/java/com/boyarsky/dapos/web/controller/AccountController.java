package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.web.API;
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
