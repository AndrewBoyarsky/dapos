package com.boyarsky.dapos.core.config;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class BlockchainConfig {
    //nosonar
    private volatile HeightConfig currentConfig;

    private Map<Long, HeightConfig> allConfigs = new HashMap<>();
    private Set<Long> updateHeights = new HashSet<>();
    private int chainId;
    private String chainName;
    private long maxSupply;
    private long oneCoinFractions;

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
        Map<Long, HeightConfig> map = spec.getHeightConfigs()
                .stream()
                .collect(Collectors.toMap(
                        HeightConfig::getHeight, Function.identity(), (prev, newC) -> {
                            log.warn("Duplicate found for height {}, will use {}", prev.getHeight(), newC);
                            return newC;
                        }));
        BigInteger totalSupply = BigInteger.valueOf(spec.getMaxSupply()).multiply(BigInteger.valueOf(spec.getOneCoinFractions()));
        try {
            this.maxSupply = totalSupply.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Total max supply is bounded by " + Long.MAX_VALUE + ", but configured max supply is " + totalSupply.toString());
        }
        updateHeights.addAll(map.keySet());

        this.allConfigs.putAll(map);
        this.chainId = spec.getChainId();
        this.chainName = spec.getChainName();
        this.oneCoinFractions = spec.getOneCoinFractions();
        this.maxSupply = spec.getMaxSupply();
    }

    public long getOneCoinFractions() {
        return oneCoinFractions;
    }

    public HeightConfig tryUpdateForHeight(long height) {
        if (updateHeights.contains(height)) {
            HeightConfig newConfig = allConfigs.get(height);
            this.currentConfig = newConfig.configureNullValuesFrom(currentConfig);
            return this.currentConfig;
        }
        return null;
    }

    public HeightConfig init(long height) {
        HeightConfig newConfig = this.allConfigs.entrySet()
                .stream()
                .filter(e -> e.getKey() <= height)
                .sorted((o1, o2) -> -Long.compare(o2.getKey(), o1.getKey()))
                .map(Map.Entry::getValue)
                .reduce((prev, newC) -> newC.configureNullValuesFrom(prev))
                .orElseThrow(() -> new RuntimeException("For height " + height + " unable to find config from " + allConfigs));
        this.currentConfig = newConfig;
        return newConfig;
    }

//    HeightConfig findConfigForHeight(long height) {
//        return this.allConfigs.entrySet()
//                .stream()
//                .filter(e -> e.getKey() <= height + 1)
//                .sorted((o1, o2) -> Long.compare(o2.getKey(), o1.getKey()))
//                .limit(1)
//                .map(Map.Entry::getValue)
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("For height " + height + " unable to find config from " + allConfigs));
//    }

    public Long getMaxSize() {
        return currentConfig.getMaxSize();
    }

    public Long getMaxGas() {
        return currentConfig.getMaxGas();
    }

    public Long getMaxEvidenceAge() {
        return currentConfig.getMaxEvidenceAge();
    }

    public Long getMaxValidators() {
        return currentConfig.getMaxValidators();
    }

    public Long getBlockReward() {
        return currentConfig.getBlockReward();
    }

    public Long getAbsentPeriod() {
        return currentConfig.getAbsentPeriod();
    }

    public Long getMaxValidatorVotes() {
        return currentConfig.getMaxValidatorVotes();
    }

    public Long getMinVoteStake() {
        return currentConfig.getMinVoteStake();
    }

    public BigDecimal getByzantinePunishment() {
        return currentConfig.getByzantinePunishment();
    }

    public BigDecimal getAbsentPunishment() {
        return currentConfig.getAbsentPunishment();
    }
}
