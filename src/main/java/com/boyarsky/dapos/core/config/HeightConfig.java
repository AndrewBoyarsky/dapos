package com.boyarsky.dapos.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeightConfig {
    private Long height;
    private Long maxSize;
    private Long maxGas;
    private Long maxEvidenceAge;
    private Long maxValidators;
    private Long blockReward;
    private Long absentPeriod;
    private Long maxValidatorVotes;
    private Long minVoteStake;
    private BigDecimal byzantinePunishment;
    private BigDecimal absentPunishment;

    HeightConfig configureNullValuesFrom(HeightConfig config) {
        maxSize = requireAtLeastOneValue(maxSize, config.getMaxSize());
        maxGas = requireAtLeastOneValue(maxGas, config.getMaxGas());
        maxEvidenceAge = requireAtLeastOneValue(maxEvidenceAge, config.getMaxEvidenceAge());
        maxValidators = requireAtLeastOneValue(maxValidators, config.getMaxValidators());
        blockReward = requireAtLeastOneValue(blockReward, config.getBlockReward());
        absentPeriod = requireAtLeastOneValue(absentPeriod, config.getAbsentPeriod());
        maxValidatorVotes = requireAtLeastOneValue(maxValidatorVotes, config.getMaxValidatorVotes());
        minVoteStake = requireAtLeastOneValue(minVoteStake, config.getMinVoteStake());
        byzantinePunishment = requireAtLeastOneValue(byzantinePunishment, config.getByzantinePunishment());
        absentPunishment = requireAtLeastOneValue(absentPunishment, config.getAbsentPunishment());
        return this;
    }

    private <T> T requireAtLeastOneValue(T current, T defaultV) {
        return Objects.requireNonNull(Objects.requireNonNullElse(current, defaultV));
    }
}
