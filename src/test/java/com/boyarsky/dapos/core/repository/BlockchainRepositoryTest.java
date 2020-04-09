package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.StoreExtension;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainRepositoryTest {
    @RegisterExtension
    static StoreExtension extension = new StoreExtension();

    @Test
    void getLastBlock_nothing_stored() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        LastSuccessBlockData lastBlock = blockchainRepository.getLastBlock();
        assertNull(lastBlock);
    }


    @Test
    void getLastBlock() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
        });
        LastSuccessBlockData lastBlock = blockchainRepository.getLastBlock();
        assertEquals(new LastSuccessBlockData(Convert.parseHexString("00ff"), 12), lastBlock);
    }

    @Test
    void getLastBlock_duplicates_exist() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        extension.getStore().executeInTransaction(txn -> {
            Entity lastBlock = txn.newEntity("lastBlock");
            lastBlock.setProperty("hash", "00ff");
            lastBlock.setProperty("height", 12L);
            Entity anotherBlock = txn.newEntity("lastBlock");
            anotherBlock.setProperty("hash", "00ffff00abbcbb");
            anotherBlock.setProperty("height", 13L);
        });
        assertThrows(RuntimeException.class, blockchainRepository::getLastBlock);
    }

    @Test
    void insert_not_exist_require_tx_flush() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        blockchainRepository.insert(toInsert, extension.getStore().getCurrentTransaction());
        assertTrue(extension.getStore().getCurrentTransaction().flush());
        LastSuccessBlockData lastBlock = blockchainRepository.getLastBlock();
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_not_exist_no_flush() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("0ab23f"), 100);
        blockchainRepository.insert(toInsert, extension.getStore().getCurrentTransaction());
        LastSuccessBlockData lastBlock = blockchainRepository.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert, lastBlock);
    }

    @Test
    void insert_alreadyExist() {
        BlockchainRepository blockchainRepository = new BlockchainRepository(extension.getStore());
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        LastSuccessBlockData toInsert2 = new LastSuccessBlockData(Convert.parseHexString("00ffff00"), 13);
        blockchainRepository.insert(toInsert, extension.getStore().getCurrentTransaction());
        blockchainRepository.insert(toInsert2, extension.getStore().getCurrentTransaction());

        LastSuccessBlockData lastBlock = blockchainRepository.getLastBlock(extension.getStore().getCurrentTransaction());
        assertEquals(toInsert2, lastBlock);
    }
}