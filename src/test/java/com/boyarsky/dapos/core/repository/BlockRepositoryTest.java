package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ContextConfiguration(classes = {RepoTest.Config.class, BlockRepositoryTest.class})
@ComponentScan("com.boyarsky.dapos.core.repository.block")
class BlockRepositoryTest extends RepoTest {
    @Autowired
    private BlockRepository repository;

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
        assertThrows(RuntimeException.class, repository::getLastBlock);
    }

    @Test
    void insert_no_tx() {
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("0ab23f"), 100);
        assertThrows(IllegalStateException.class, () -> repository.insert(toInsert));
    }

    @Test
    void insert_alreadyExist() {
        manager.begin();
        LastSuccessBlockData toInsert = new LastSuccessBlockData(Convert.parseHexString("00ff"), 12);
        LastSuccessBlockData toInsert2 = new LastSuccessBlockData(Convert.parseHexString("00ffff00"), 13);
        repository.insert(toInsert);
        repository.insert(toInsert2);
        manager.commit();
        LastSuccessBlockData lastBlock = repository.getLastBlock();
        assertEquals(toInsert2, lastBlock);
    }
}