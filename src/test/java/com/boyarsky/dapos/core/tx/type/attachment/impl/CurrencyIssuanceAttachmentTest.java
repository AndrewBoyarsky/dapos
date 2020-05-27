package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;

class CurrencyIssuanceAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        AccountId id = TestUtil.generateEd25Acc().getCryptoId();
        return List.of(new CurrencyIssuanceAttachment((byte) 1, "STI", "String Swing Spring", "Super coin for those who do", id, Long.MAX_VALUE, 100, (byte) 2));
    }
}