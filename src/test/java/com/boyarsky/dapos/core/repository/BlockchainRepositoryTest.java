package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.StoreExtension;
import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockchainRepositoryTest {
    @RegisterExtension
    static StoreExtension extension = new StoreExtension();
    private XodusRepoContext context;
    private BlockchainRepository repository;

    @BeforeEach
    void setUp() {
        context = new XodusRepoContext(extension.getStore(), new TransactionManager(extension.getStore()));
        repository = new BlockchainRepository(context);
    }

    @Test
    void getLastBlock_nothing_stored() {
        LastSuccessBlockData lastBlock = repository.getLastBlock();
        assertNull(lastBlock);
    }


    @Test
    void getLastBlock() {
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
        });
        extension.getStore().getCurrentTransaction().revert(); // move tx version to the newest
        LastSuccessBlockData lastBlock = repository.getLastBlock();
        assertEquals(new LastSuccessBlockData(Convert.parseHexString("00ff"), 12), lastBlock);
    }

    @Test
    void getLastBlock_duplicates_exist() {
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
            Entity anotherBlock = txn.newEntity("lastBlock");
            anotherBlock.setProperty("hash", "00ffff00abbcbb");
            anotherBlock.setProperty("height", 13L);
        });
        extension.getStore().getCurrentTransaction().revert();
        assertThrows(RuntimeException.class, repository::getLastBlock);
    }

    @Test
    void insert_not_exist_require_tx_flush() {
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        repository.insert(toInsert);
        LastSuccessBlockData lastBlock = repository.getLastBlock();
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_not_exist_no_flush() {
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("0ab23f"), 100);
        repository.insert(toInsert);
        LastSuccessBlockData lastBlock = repository.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_alreadyExist() {
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        LastSuccessBlockData toInsert2 = new LastSuccessBlockData(Convert.parseHexString("00ffff00"), 13);
        repository.insert(toInsert);
        repository.insert(toInsert2);

        LastSuccessBlockData lastBlock = repository.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert2, lastBlock);
    }
}