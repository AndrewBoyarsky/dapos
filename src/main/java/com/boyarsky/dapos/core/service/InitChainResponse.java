package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.genesis.GenesisInitResult;
import lombok.Data;

@Data
public class InitChainResponse {
    private GenesisInitResult genesisInitResult;
    private HeightConfig config;
}
