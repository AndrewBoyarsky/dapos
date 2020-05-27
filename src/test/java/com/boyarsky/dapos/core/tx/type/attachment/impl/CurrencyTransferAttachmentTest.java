package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;

class CurrencyTransferAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(
                new CurrencyTransferAttachment((byte) 2, 1000)
        );
    }
}