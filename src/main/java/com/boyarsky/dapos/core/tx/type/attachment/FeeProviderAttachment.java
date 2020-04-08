package com.boyarsky.dapos.core.tx.type.attachment;

import com.boyarsky.dapos.core.dao.model.PartyFeeConfig;
import com.boyarsky.dapos.core.dao.model.State;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

@EqualsAndHashCode(callSuper = true)
@Data
//TODO maybe better to move all boolean flags to the beginning of buffer and store inside one integer
public class FeeProviderAttachment extends AbstractAttachment {
    private State state;
    private PartyFeeConfig fromFeeConfig;
    private PartyFeeConfig toFeeConfig;


    public FeeProviderAttachment(ByteBuffer buffer) {
        super(buffer);
        state = State.ofCode(buffer.get());
        fromFeeConfig = new PartyFeeConfig(buffer);
        toFeeConfig = new PartyFeeConfig(buffer);
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        buffer.put(state.getCode());
        fromFeeConfig.putBytes(buffer);
        toFeeConfig.putBytes(buffer);
    }

    public int size() {
        return 1 + fromFeeConfig.size() + toFeeConfig.size();
    }

}
