package com.boyarsky.dapos.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class DirConfiguration {
    private DirProvider provider;

    @Autowired
    public DirConfiguration(DirProvider provider) {
        this.provider = provider;
    }

    @Bean(name = "dbDir")
    public Path dbPath() throws IOException {
        return provider.dbPath();
    }

    @Bean(name = "dataDir")
    public Path dataPath() throws IOException {
        return provider.dataPath();
    }

    @Bean(name = "keystoreDir")
    public Path keystorePath() throws IOException {
        return provider.keystorePath();
    }
}
