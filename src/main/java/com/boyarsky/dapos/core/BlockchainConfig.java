package com.boyarsky.dapos.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockchainConfig {
    @Data
    public static class HeightConfig {
        private final long height;
        private final long maxSize;
        private final long maxGas;
        private final long maxEvidenceAge;

        @JsonCreator
        public HeightConfig(long height, long maxSize, long maxGas, long maxEvidenceAge) {
            this.height = height;
            this.maxSize = maxSize;
            this.maxGas = maxGas;
            this.maxEvidenceAge = maxEvidenceAge;
        }

    }
    private volatile HeightConfig currentConfig;

    private Map<Long, HeightConfig> allConfigs = new HashMap<>();
    private Set<Long> updateHeights = new HashSet<>();


    public BlockchainConfig(List<HeightConfig> allConfigs) {
        Map<Long, HeightConfig> map = allConfigs.stream().collect(Collectors.toMap(HeightConfig::getHeight, Function.identity()));
        updateHeights.addAll(map.keySet());
        this.allConfigs.putAll(map);
    }

    public void tryUpdateForHeight(long height) {
        if (updateHeights.contains(height)) {
            currentConfig = allConfigs.get(height);
        }
    }

    public void init(long height) {
        currentConfig = this.allConfigs.entrySet()
                .stream()
                .filter(e -> e.getKey() <= height)
                .sorted((o1, o2) -> Long.compare(o2.getKey(), o1.getKey()))
                .limit(1)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("For height " + height + " unable to find config from " + allConfigs));
    }
}
