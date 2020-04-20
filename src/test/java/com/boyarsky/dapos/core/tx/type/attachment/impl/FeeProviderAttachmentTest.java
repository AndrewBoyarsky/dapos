package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.AccountUtil;
import com.boyarsky.dapos.core.model.FeeConfig;
import com.boyarsky.dapos.core.model.PartyFeeConfig;
import com.boyarsky.dapos.core.model.State;
import com.boyarsky.dapos.core.tx.type.TxType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeeProviderAttachmentTest {

    @Test
    void serialization() {
        FeeProviderAttachment allowAllAttachment = new FeeProviderAttachment((byte) 1, State.ACTIVE, new PartyFeeConfig(true, null, null), new PartyFeeConfig(true, null, null));
        testAttachmentSerialization(allowAllAttachment, 6);

        FeeProviderAttachment restrictedAttachment = new FeeProviderAttachment((byte) 2, State.SUSPENDED, //2
                new PartyFeeConfig(true,
                        new FeeConfig(10, 2, 15, Set.of(TxType.PAYMENT, TxType.DELEGATE)), null), //27
                new PartyFeeConfig(false, null, //3
                        Map.of(new FeeConfig(5, 10, 12, Set.of()), //24
                                List.of(AccountUtil.generateEd25Acc().getCryptoId(), AccountUtil.generateEd25Acc().getCryptoId()), // 36
                                new FeeConfig(-1, -1, 12, Set.of()), // 12
                                List.of(AccountUtil.generateEd25Acc().getCryptoId()), // 19
                                new FeeConfig(-1, -1, -1, Set.of(TxType.PAYMENT)), //5
                                List.of() // 2
                        )));
        testAttachmentSerialization(restrictedAttachment, 131);
    }

    private void testAttachmentSerialization(FeeProviderAttachment testableAttachment, int expectedSize) {
        assertEquals(expectedSize, testableAttachment.size());
        ByteBuffer buffer = ByteBuffer.allocate(expectedSize);
        testableAttachment.putBytes(buffer);
        buffer.flip();
        FeeProviderAttachment deserializedAttachment = new FeeProviderAttachment(buffer);
        assertEquals(testableAttachment, deserializedAttachment);
    }

}