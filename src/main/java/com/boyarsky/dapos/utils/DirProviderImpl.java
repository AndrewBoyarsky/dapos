package com.boyarsky.dapos.utils;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DirProviderImpl implements DirProvider {
    public static final String appName = "dapos";
    public static final String appDir = "." + appName;
    public static final String appDataDir = "app-data";
    public static final String appDbDir = "app-db";
    public static final String appKeystore = "app-keystore";
    private Path homeDir;

    public DirProviderImpl(String home) throws IOException {
        this.homeDir = Paths.get(home).resolve(appDir);
        Files.createDirectories(homeDir);
    }

    public DirProviderImpl() throws IOException {
        this(System.getProperty("user.home"));
    }


    @Override
    public Path dbPath() throws IOException {
        Path dbDir = homeDir.resolve(appDbDir);
        Files.createDirectories(dbDir);
        return dbDir;
    }

    @Override
    public Path dataPath() throws IOException {
        Path dataDir = homeDir.resolve(appDataDir);
        Files.createDirectories(dataDir);
        return dataDir;
    }

    @Override
    public Path keystorePath() throws IOException {
        Path keystoreDir = homeDir.resolve(appKeystore);
        Files.createDirectories(keystoreDir);
        return keystoreDir;
    }
}
