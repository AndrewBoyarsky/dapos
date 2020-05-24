package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EndBlockResponse {
    private HeightConfig newConfig;
    private List<ValidatorEntity> validators = new ArrayList<>();
}
