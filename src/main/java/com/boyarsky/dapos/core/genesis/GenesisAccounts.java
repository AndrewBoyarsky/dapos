package com.boyarsky.dapos.core.genesis;

import lombok.Data;

import java.util.List;

@Data
class GenesisAccounts {
    private List<GenesisAccount> accounts;

    @Data
    static class GenesisAccount {
        private String accountId;
        private String publicKey;
        private Long balance;
    }
}
