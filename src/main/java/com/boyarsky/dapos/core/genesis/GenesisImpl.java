package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.utils.Convert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GenesisImpl implements Genesis {
    private static final String DEFAULT_GENESIS_PATH = "genesis.json";
    private AccountService accountService;
    private ValidatorService validatorService;
    private ObjectMapper mapper;
    private String genesisPath;

    @Autowired
    public GenesisImpl(AccountService accountService, ValidatorService validatorService, ObjectMapper mapper) {
        this(accountService, validatorService, mapper, DEFAULT_GENESIS_PATH);
    }

    public GenesisImpl(AccountService accountService, ValidatorService validatorService, ObjectMapper mapper, String genesisPath) {
        this.accountService = accountService;
        this.mapper = mapper;
        this.validatorService = validatorService;
        this.genesisPath = genesisPath;
    }

    @Override
    public GenesisInitResponse initialize() {
        ClassPathResource resource = new ClassPathResource(genesisPath);
        GenesisData genesisData;
        try {
            genesisData = mapper.readValue(resource.getURL(), GenesisData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<GenesisData.GenesisAccount> accounts = genesisData.getAccounts();
        for (GenesisData.GenesisAccount account : accounts) {
            accountService.save(new Account(new AccountId(account.getAccountId()), Convert.parseHexString(account.getPublicKey()), account.getBalance(), Account.Type.ORDINARY));
        }
        List<GenesisData.ValidatorDefinition> validators = genesisData.getValidators();
        List<ValidatorEntity> entities = new ArrayList<>();
        for (GenesisData.ValidatorDefinition validator : validators) {
            ValidatorEntity validatorEntity = validatorService.registerValidator(Convert.parseHexString(validator.getPublicKey()), validator.getFee(), new AccountId(validator.getRewardId()), true, 0);
            validatorEntity.setVotePower(10); // just init non null vote power to remain in validator set
            entities.add(validatorEntity);
        }
        GenesisInitResponse response = new GenesisInitResponse();
        response.setNumberOfAccount(accounts.size());
        response.setValidatorEntities(entities);
        return response;
    }
}
