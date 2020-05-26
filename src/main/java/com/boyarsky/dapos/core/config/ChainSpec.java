package com.boyarsky.dapos.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChainSpec {
    private String chainName;
    private int chainId;
    private long maxSupply;
    private long oneCoinFractions;
    private List<HeightConfig> heightConfigs = new ArrayList<>();
}
