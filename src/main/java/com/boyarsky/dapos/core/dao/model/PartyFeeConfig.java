package com.boyarsky.dapos.core.dao.model;

import com.boyarsky.dapos.account.AccountId;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyFeeConfig implements ByteSerializable {
    private boolean whitelistAll;
    private FeeConfig rootConfig;
    private Map<AccountId, FeeConfig> configs = new LinkedHashMap<>();

    public PartyFeeConfig(ByteBuffer buffer) {
        whitelistAll = buffer.get() == 1;
        if (whitelistAll) {
            boolean rootConfigExist = buffer.get() == 1;
            if (rootConfigExist) {
                rootConfig = new FeeConfig(buffer);
            }
        } else {
            short whitelistedSize = buffer.getShort();
            for (int i = 0; i < whitelistedSize; i++) {
                configs.put(AccountId.fromBytes(buffer), new FeeConfig(buffer));
            }
        }
    }

    @Override
    public int size() {
        return 1 + (whitelistAll ? 1 : 0) + (rootConfig == null ? 0 :  rootConfig.size()) + (whitelistAll ? 0 : 2 + configs.entrySet().stream().mapToInt((e)-> e.getKey().size() + e.getValue().size()).sum());
    }

    @Override
    public void putBytes(ByteBuffer buffer) {
        if (whitelistAll) {
            buffer.put((byte) 1);
            if (rootConfig != null) {
                buffer.put((byte) 1);
                rootConfig.putBytes(buffer);
            } else {
                buffer.put((byte) 0);
            }
        } else {
            buffer.put((byte) 0);
            buffer.putShort((short) configs.size());
            configs.forEach((key, v)-> {
                key.putBytes(buffer);
                v.putBytes(buffer);
            });
        }
    }
}
