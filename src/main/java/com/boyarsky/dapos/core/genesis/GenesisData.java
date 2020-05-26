package com.boyarsky.dapos.core.genesis;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
class GenesisData {
    private List<GenesisAccount> accounts;
    private List<ValidatorDefinition> validators;

    @Data
    static class GenesisAccount {
        private String accountId;
        private String publicKey;
        private Long balance;
    }

    @Data
    static class ValidatorDefinition {
        private BigDecimal fee;
        private long power;
        private String publicKey;
        private String rewardId;
    }
}
