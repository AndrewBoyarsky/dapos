package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;
import java.util.Map;

class CurrencyMultiAccountAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(
                new CurrencyMultiAccountAttachment((byte) 1, Map.of(
                        CryptoUtils.generateBitcoinWallet().getAccount(), 222L
                ), 333L),
                new CurrencyMultiAccountAttachment((byte) 1, Map.of(
                        CryptoUtils.generateBitcoinWallet().getAccount(), 323L,
                        CryptoUtils.generateEthWallet().getAccount(), 19999999999L
                ), 222L)
        );
    }
}