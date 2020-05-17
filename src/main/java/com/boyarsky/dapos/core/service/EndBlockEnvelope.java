package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.config.HeightConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EndBlockEnvelope {
    private HeightConfig newConfig;
    private List<ValidatorUpdate> validators = new ArrayList<>();
}
