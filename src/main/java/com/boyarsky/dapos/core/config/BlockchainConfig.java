package com.boyarsky.dapos.core.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockchainConfig {
    private volatile HeightConfig currentConfig;

    private Map<Long, HeightConfig> allConfigs = new HashMap<>();
    private Set<Long> updateHeights = new HashSet<>();


    public BlockchainConfig(ChainSpec spec) {
        Map<Long, HeightConfig> map = spec.getConfigs().stream().collect(Collectors.toMap(HeightConfig::getHeight, Function.identity()));
        updateHeights.addAll(map.keySet());
        this.allConfigs.putAll(map);
    }

    public boolean tryUpdateForHeight(long height) {
        if (updateHeights.contains(height)) {
            currentConfig = allConfigs.get(height);
            return true;
        }
        return false;
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
        this.currentConfig = newConfig;
    }

    public HeightConfig getCurrentConfig() {
        return currentConfig;
    }
}
