package com.boyarsky.dapos.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Objects;

@Configuration
public class ChaingSpringConfiguration {

    @Autowired
    ObjectMapper mapper;

    @Bean
    BlockchainConfig config() throws IOException {
        ChainSpec chainSpec = mapper.readValue(Objects.requireNonNull(getClass().getClassLoader().getResource("blockchain-config.json")), ChainSpec.class);
        return new BlockchainConfig(chainSpec);
    }
}
