package com.boyarsky.dapos.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DirProvider {
    public static final String appName = "dapos";
    public static final String appDir = "." + appName;
    public static final String appDataDir = "app-data";
    public static final String appDbDir = "app-db";
    public static final String appKeystore = "app-keystore";
    private Path homeDir;

    public DirProvider() throws IOException {
        this.homeDir = Paths.get(System.getProperty("user.home")).resolve(appDir);
        Files.createDirectories(homeDir);
    }


    @Bean(name = "dbDir")
    public Path dbPath() throws IOException {
        Path dbDir = homeDir.resolve(appDbDir);
        Files.createDirectories(dbDir);
        return dbDir;
    }
    @Bean(name = "dataDir")
    public Path dataPath() throws IOException {
        Path dataDir = homeDir.resolve(appDataDir);
        Files.createDirectories(dataDir);
        return dataDir;
    }

    @Bean(name = "keystoreDir")
    public Path keystorePath() throws IOException {
        Path keystoreDir = homeDir.resolve(appKeystore);
        Files.createDirectories(keystoreDir);
        return keystoreDir;
    }
}
