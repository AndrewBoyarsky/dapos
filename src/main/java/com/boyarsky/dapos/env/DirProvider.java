package com.boyarsky.dapos.env;

import java.io.IOException;
import java.nio.file.Path;

public interface DirProvider {
    Path dbPath() throws IOException;

    Path dataPath() throws IOException;

    Path keystorePath() throws IOException;
}
