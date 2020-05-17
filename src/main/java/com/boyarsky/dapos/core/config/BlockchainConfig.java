package com.boyarsky.dapos.core.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockchainConfig {
    private volatile HeightConfig currentConfig;

    private Map<Long, HeightConfig> allConfigs = new HashMap<>();
    private Set<Long> updateHeights = new HashSet<>();
    private int chainId;
    private String chainName;
    private long maxSupply;

    public int getChainId() {
        return chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public long getMaxSupply() {
        return maxSupply;
    }

    public BlockchainConfig(ChainSpec spec) {
        Map<Long, HeightConfig> map = spec.getHeightConfigs().stream().collect(Collectors.toMap(HeightConfig::getHeight, Function.identity()));
        updateHeights.addAll(map.keySet());
        this.allConfigs.putAll(map);
        this.chainId = spec.getChainId();
        this.chainName = spec.getChainName();
        this.maxSupply = spec.getMaxSupply();
    }

    public boolean tryUpdateForHeight(long height) {
        if (updateHeights.contains(height)) {
            currentConfig = allConfigs.get(height);
            return true;
        }
        return false;
    }

    public long maxValidatorsForHeight(long height) {
        if (currentConfig.getHeight() < height) {
            return currentConfig.getMaxValidators();
        } else if (height <= 1) {
            return allConfigs.get(height).getMaxValidators();
        } else {
            return findConfigForHeight(height).getMaxValidators();
        }
    }

    public void init(long height) {
        HeightConfig newConfig = this.allConfigs.entrySet()
                .stream()
                .filter(e -> e.getKey() <= height)
                .sorted((o1, o2) -> Long.compare(o2.getKey(), o1.getKey()))
                .limit(1)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("For height " + height + " unable to find config from " + allConfigs));
        if (newConfig.getMaxEvidenceAge() == null) {
            newConfig.setMaxEvidenceAge(currentConfig.getMaxEvidenceAge());
        }
        if (newConfig.getMaxGas() == null) {
            newConfig.setMaxGas(currentConfig.getMaxGas());
        }
        if (newConfig.getMaxSize() == null) {
            newConfig.setMaxSize(currentConfig.getMaxSize());
        }
        if (newConfig.getAbsentPeriod() == null) {
            newConfig.setAbsentPeriod(currentConfig.getAbsentPeriod());
        }
        this.currentConfig = newConfig;
    }

    HeightConfig findConfigForHeight(long height) {
        return this.allConfigs.entrySet()
                .stream()
                .filter(e -> e.getKey() <= height + 1)
                .sorted((o1, o2) -> Long.compare(o2.getKey(), o1.getKey()))
                .limit(1)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("For height " + height + " unable to find config from " + allConfigs));
    }

    public HeightConfig getCurrentConfig() {
        return currentConfig;
    }
}
