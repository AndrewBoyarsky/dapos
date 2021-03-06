package com.boyarsky.dapos.utils;

import com.apollocurrency.aplwallet.apl.util.FileUtils;
import com.boyarsky.dapos.config.DirConfiguration;
import com.boyarsky.dapos.env.DirProvider;
import com.boyarsky.dapos.env.DirProviderImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextHierarchy(value = {
        @ContextConfiguration(classes = DirProviderTest.Config.class),
        @ContextConfiguration(classes = DirConfiguration.class),
})
class DirProviderTest {
    static Path tempDir;

    @Autowired
    DirProvider provider;
    @Autowired
    @Qualifier("dbDir")
    Path dbDir;
    @Autowired
    @Qualifier("keystoreDir")
    Path keystoreDir;
    @Autowired
    @Qualifier("dataDir")
    Path dataDir;

    @Test
    void providePaths() {
        assertEquals(tempDir.resolve(".dapos/app-keystore"), keystoreDir);
        assertTrue(Files.exists(keystoreDir));
        assertEquals(tempDir.resolve(".dapos/app-data"), dataDir);
        assertTrue(Files.exists(dataDir));
        assertEquals(tempDir.resolve(".dapos/app-db"), dbDir);
        assertTrue(Files.exists(dbDir));
        FileUtils.clearDirectorySilently(tempDir);
        FileUtils.deleteFileIfExistsQuietly(tempDir);
    }

    public static class Config {
        @Bean
        DirProvider prov() throws IOException {
            tempDir = Files.createTempDirectory("dirprov-test");
            return new DirProviderImpl(tempDir.toAbsolutePath().toString());
        }
    }
}