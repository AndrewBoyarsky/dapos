package com.boyarsky.dapos.core.config;

import lombok.Data;

@Data
public class HeightConfig {
    private Long height;
    private Long maxSize;
    private Long maxGas;
    private Long maxEvidenceAge;
    private Long maxValidators;
    private Long blockReward;
    private Long absentPeriod;
}
