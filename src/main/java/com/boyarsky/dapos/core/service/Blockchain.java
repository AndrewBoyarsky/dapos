package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Blockchain {
    private volatile long currentHeight;
    private final BlockRepository blockRepository;

    @Autowired
    public Blockchain(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public long getCurrentBlockHeight() {
        return currentHeight;
    }

    public void beginBlock(long height) {
        this.currentHeight = height;
    }

    public void addNewBlock(byte[] hash) {
        blockRepository.insert(new LastSuccessBlockData(hash, currentHeight));
    }

    public LastSuccessBlockData getLastBlock() {
        return blockRepository.getLastBlock();
    }
}
