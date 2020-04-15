package com.boyarsky.dapos.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChainSpec {
    private String chainName;
    private String chainId;
    private List<HeightConfig> heightConfigs = new ArrayList<>();
}
