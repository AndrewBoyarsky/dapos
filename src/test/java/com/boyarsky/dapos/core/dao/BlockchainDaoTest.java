package com.boyarsky.dapos.core.dao;

import com.boyarsky.dapos.StoreExtension;
import com.boyarsky.dapos.core.dao.model.LastSuccessBlockData;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.StoreTransactionalExecutable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainDaoTest {
    @RegisterExtension
    static StoreExtension extension = new StoreExtension();

    @Test
    void getLastBlock_nothing_stored() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        LastSuccessBlockData lastBlock = blockchainDao.getLastBlock();
        assertNull(lastBlock);
    }


    @Test
    void getLastBlock() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
        });
        LastSuccessBlockData lastBlock = blockchainDao.getLastBlock();
        assertEquals(new LastSuccessBlockData(Convert.parseHexString("00ff"), 12), lastBlock);
    }

    @Test
    void getLastBlock_duplicates_exist() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
            Entity anotherBlock = txn.newEntity("lastBlock");
            anotherBlock.setProperty("hash", "00ffff00abbcbb");
            anotherBlock.setProperty("height", 13L);
        });
        assertThrows(RuntimeException.class, blockchainDao::getLastBlock);
    }

    @Test
    void insert_not_exist_require_tx_flush() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        blockchainDao.insert(toInsert, extension.getStore().getCurrentTransaction());
        assertTrue(extension.getStore().getCurrentTransaction().flush());
        LastSuccessBlockData lastBlock = blockchainDao.getLastBlock();
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_not_exist_no_flush() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("0ab23f"), 100);
        blockchainDao.insert(toInsert, extension.getStore().getCurrentTransaction());
        LastSuccessBlockData lastBlock = blockchainDao.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_alreadyExist() {
        BlockchainDao blockchainDao = new BlockchainDao(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        LastSuccessBlockData toInsert2 = new LastSuccessBlockData(Convert.parseHexString("00ffff00"), 13);
        blockchainDao.insert(toInsert, extension.getStore().getCurrentTransaction());
        blockchainDao.insert(toInsert2, extension.getStore().getCurrentTransaction());

        LastSuccessBlockData lastBlock = blockchainDao.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert2, lastBlock);
    }
}