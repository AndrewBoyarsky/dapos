package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenesisInitResponse {
    private List<ValidatorEntity> validatorEntities = new ArrayList<>();
    private int numberOfAccount;
}
