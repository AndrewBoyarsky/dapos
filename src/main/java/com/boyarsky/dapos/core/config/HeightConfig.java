package com.boyarsky.dapos.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class HeightConfig {
    private Long height;
    private Long maxSize;
    private Long maxGas;
    private Long maxEvidenceAge;
}
