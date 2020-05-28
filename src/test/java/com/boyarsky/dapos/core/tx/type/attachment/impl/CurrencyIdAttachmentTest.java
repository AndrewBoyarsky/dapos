package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;

class CurrencyIdAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(
                new CurrencyIdAttachment((byte) 2, 1000)
        );
    }
}