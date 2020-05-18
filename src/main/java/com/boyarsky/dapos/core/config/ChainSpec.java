package com.boyarsky.dapos.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChainSpec {
    private String chainName;
    private int chainId;
    private long maxSupply;
    private long oneCoinFractions;
    private List<HeightConfig> heightConfigs = new ArrayList<>();
}
