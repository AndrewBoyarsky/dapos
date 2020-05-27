package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.util.List;
import java.util.Map;
import java.util.Set;

class FeeProviderAttachmentTest extends AbstractAttachmentTest {

    @Override
    protected List<Attachment> toTest() {
        return List.of(
                new FeeProviderAttachment((byte) 1, State.ACTIVE, new PartyFeeConfig(true, null, null), new PartyFeeConfig(true, null, null)),
                new FeeProviderAttachment((byte) 2, State.SUSPENDED, //2
                        new PartyFeeConfig(true,
                                new FeeConfig(10, 2, 15, Set.of(TxType.PAYMENT, TxType.VOTE)), null), //27
                        new PartyFeeConfig(false, null, //3
                                Map.of(new FeeConfig(5, 10, 12, Set.of()), //24
                                        List.of(TestUtil.generateEd25Acc().getCryptoId(), TestUtil.generateEd25Acc().getCryptoId()), // 36
                                        new FeeConfig(-1, -1, 12, Set.of()), // 12
                                        List.of(TestUtil.generateEd25Acc().getCryptoId()), // 19
                                        new FeeConfig(-1, -1, -1, Set.of(TxType.PAYMENT)), //5
                                        List.of() // 2
                                )))
        );
    }
}