package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;
import java.util.Map;

class MultiAccountAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(
                new MultiAccountAttachment((byte) -1, Map.of(
                        CryptoUtils.generateEd25Wallet().getAccount(), 90L,
                        CryptoUtils.generateEd25Wallet().getAccount(), 1200L,
                        CryptoUtils.generateValidatorWallet().getAccount(), 333L,
                        CryptoUtils.generateBitcoinWallet().getAccount(), 334L,
                        CryptoUtils.generateEthWallet().getAccount(), 32300L,
                        CryptoUtils.generateEthWallet().getAccount(), Long.MAX_VALUE
                )),
                new MultiAccountAttachment((byte) -1, Map.of()),
                new MultiAccountAttachment((byte) -1, Map.of(CryptoUtils.generateEd25Wallet().getAccount(), 3L))
        );
    }
}