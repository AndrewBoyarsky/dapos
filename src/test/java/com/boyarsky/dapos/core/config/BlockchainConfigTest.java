package com.boyarsky.dapos.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class BlockchainConfigTest {
    HeightConfig after10 = new HeightConfig(10L, 100L, 15000L, 90L, 10L, 1300L, 10L, 15L, 300L, new BigDecimal("5.23"), new BigDecimal("3.22"));
    @Autowired
    BlockchainConfig config;

    @Test
    void tryUpdateForHeight_no_update() {
        HeightConfig heightConfig = config.tryUpdateForHeight(3);

        assertNull(heightConfig);
    }

    @Test
    void testCreate_using_big_maxSupply() {
        assertThrows(IllegalArgumentException.class, () -> new BlockchainConfig(new ChainSpec("unit test", 2, Long.MAX_VALUE, 2, List.of())));
    }

    @Test
    void tryUpdateFlow() {
        HeightConfig initConfig = config.init(1);
        HeightConfig synthetic = new HeightConfig(1L, 100L, 25L, 90L, 10L, 1900L, 10L, 15L, 900L, new BigDecimal("5.23"), new BigDecimal("2.33"));
        assertEquals(initConfig, initConfig);

        HeightConfig at2 = config.tryUpdateForHeight(2);

        synthetic.setBlockReward(1300L);
        synthetic.setHeight(2L);
        assertEquals(synthetic, at2);

        HeightConfig at10 = config.tryUpdateForHeight(10);

        synthetic.setHeight(10L);
        synthetic.setMinVoteStake(300L);
        synthetic.setMaxGas(15000L);
        synthetic.setAbsentPunishment(new BigDecimal("3.22"));
        assertEquals(synthetic, at10);

        HeightConfig at15 = config.tryUpdateForHeight(15);

        synthetic.setHeight(15L);
        synthetic.setByzantinePunishment(new BigDecimal("2.22"));
        synthetic.setAbsentPeriod(90L);
        synthetic.setMaxValidatorVotes(100L);
        assertEquals(synthetic, at15);
    }

    @Test
    void testUpdate_whenRestoredAtIntermediateHeight() {
        HeightConfig initConfig = this.config.init(14);

        assertEquals(after10, initConfig);
        assertEquals(after10.getAbsentPeriod(), config.getAbsentPeriod());
        assertEquals(after10.getMaxEvidenceAge(), config.getMaxEvidenceAge());
        assertEquals(after10.getBlockReward(), config.getBlockReward());
        assertEquals(after10.getMaxGas(), config.getMaxGas());
        assertEquals(after10.getMaxSize(), config.getMaxSize());
        assertEquals(after10.getMaxValidators(), config.getMaxValidators());
        assertEquals(after10.getMaxValidatorVotes(), config.getMaxValidatorVotes());
        assertEquals(after10.getByzantinePunishment(), config.getByzantinePunishment());
        assertEquals(after10.getAbsentPunishment(), config.getAbsentPunishment());
        assertEquals(after10.getAbsentPeriod(), config.getAbsentPeriod());
    }

    @TestConfiguration
    @ComponentScan("com.boyarsky.dapos.core.config")
    static class Config {
        @Bean
        ObjectMapper mapper() {
            return new ObjectMapper();
        }
    }

}