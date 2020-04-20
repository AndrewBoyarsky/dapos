package com.boyarsky.dapos.core.tx.type.attachment.impl;

import com.boyarsky.dapos.core.model.PartyFeeConfig;
import com.boyarsky.dapos.core.model.State;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public class FeeProviderAttachment extends AbstractAttachment {
    private final State state;
    private final PartyFeeConfig fromFeeConfig;
    private final PartyFeeConfig toFeeConfig;

    public FeeProviderAttachment(byte version, State state, PartyFeeConfig fromFeeConfig, PartyFeeConfig toFeeConfig) {
        super(version);
        this.state = state;
        this.fromFeeConfig = fromFeeConfig;
        this.toFeeConfig = toFeeConfig;
    }

    public FeeProviderAttachment(ByteBuffer buffer) {
        super(buffer);
        state = State.ofCode(buffer.get());
        fromFeeConfig = new PartyFeeConfig(buffer);
        toFeeConfig = new PartyFeeConfig(buffer);
    }

    @Override
    public void putMyBytes(ByteBuffer buffer) {
        buffer.put(state.getCode());
        fromFeeConfig.putBytes(buffer);
        toFeeConfig.putBytes(buffer);
    }

    @Override
    public int mySize() {
        return 1 + fromFeeConfig.size() + toFeeConfig.size();
    }

}
