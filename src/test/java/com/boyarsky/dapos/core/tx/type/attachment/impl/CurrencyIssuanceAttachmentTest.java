package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;

class CurrencyIssuanceAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(new CurrencyIssuanceAttachment((byte) 1, "STI", "String Swing Spring", "Super coin for those who do", 100, (byte) 2));
    }
}