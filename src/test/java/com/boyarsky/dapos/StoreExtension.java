package com.boyarsky.dapos;

import com.apollocurrency.aplwallet.apl.util.FileUtils;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.nio.file.Files;

public class StoreExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    PersistentEntityStore store;
    File tempDir;
    StoreTransaction currentTransaction;
    boolean startTx = true;

    public StoreExtension(boolean startTx) {
        this.startTx = startTx;
    }

    public StoreExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        tempDir = Files.createTempDirectory("tempstore").toFile();
        store = PersistentEntityStores.newInstance(tempDir);
//        store.executeInExclusiveTransaction((tx) -> store.registerCustomPropertyType(tx, ComparableByteArray.class, new ComparableByteArrayBinding()));
    }

    public PersistentEntityStore getStore() {
        return store;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        store.clear();
        store.close();
        FileUtils.clearDirectorySilently(tempDir.toPath());
        Files.deleteIfExists(tempDir.toPath());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (startTx) {
            currentTransaction = store.beginTransaction();
        }
//        store.executeInExclusiveTransaction((tx) -> store.registerCustomPropertyType(tx, ComparableByteArray.class, new ComparableByteArrayBinding()));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (startTx) {
            currentTransaction.abort();
        }
        store.clear();
    }
}
