package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.account.AccountService;
import com.boyarsky.dapos.utils.Convert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GenesisImpl implements Genesis {
    private static final String DEFAULT_GENESIS_PATH = "genesis-accounts.json";
    private AccountService accountService;
    private ObjectMapper mapper;
    private String genesisPath;

    @Autowired
    public GenesisImpl(AccountService accountService, ObjectMapper mapper) {
        this(accountService, mapper, DEFAULT_GENESIS_PATH);
    }

    public GenesisImpl(AccountService accountService, ObjectMapper mapper, String genesisPath) {
        this.accountService = accountService;
        this.mapper = mapper;
        this.genesisPath = genesisPath;
    }

    @Override
    public int initialize() {
        ClassPathResource resource = new ClassPathResource(genesisPath);
        GenesisAccounts genesisAccounts;
        try {
            genesisAccounts = mapper.readValue(resource.getURL(), GenesisAccounts.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<GenesisAccounts.GenesisAccount> accounts = genesisAccounts.getAccounts();
        for (GenesisAccounts.GenesisAccount account : accounts) {
            accountService.save(new Account(new AccountId(account.getAccountId()), Convert.parseHexString(account.getPublicKey()), account.getBalance(), Account.Type.ORDINARY));
        }
        return accounts.size();
    }
}
