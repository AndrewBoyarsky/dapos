package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.genesis.GenesisInitResponse;
import lombok.Data;

@Data
public class InitChainResponse {
    private GenesisInitResponse genesisInitResponse;
    private HeightConfig config;
}
