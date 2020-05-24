package com.boyarsky.dapos.core.genesis;

import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenesisInitResult {
    private List<ValidatorEntity> validatorEntities = new ArrayList<>();
    private int numberOfAccount;
}
