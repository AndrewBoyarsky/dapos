package com.boyarsky.dapos.core.model;

import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.repository.XodusSerializable;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeProvider implements ByteSerializable, XodusSerializable {
    private long id;
    private AccountId account;
    private long balance;
    private State state;
    private PartyFeeConfig fromFeeConfig;
    private PartyFeeConfig toFeeConfig;

    @Override
    public int size() {
        return 8 + account.size() + 8 + 1 + fromFeeConfig.size() + toFeeConfig.size();
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        buffer.putLong(id);
        account.putBytes(buffer);
        buffer.put(state.getCode());
        fromFeeConfig.putBytes(buffer);
        toFeeConfig.putBytes(buffer);
    }

    @Override
    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(size());
        putBytes(buffer);
        buffer.flip();
        return buffer;
    }
}
