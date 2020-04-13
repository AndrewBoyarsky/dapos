package com.boyarsky.dapos.core.model;

import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyFeeConfig implements ByteSerializable {
    private boolean whitelistAll;
    private FeeConfig rootConfig;
    private Map<FeeConfig, List<AccountId>> configs = new LinkedHashMap<>();

    public PartyFeeConfig(ByteBuffer buffer) {
        whitelistAll = buffer.get() == 1;
        if (whitelistAll) {
            boolean rootConfigExist = buffer.get() == 1;
            if (rootConfigExist) {
                rootConfig = new FeeConfig(buffer);
            }
        } else {
            short feeConfigs = buffer.getShort();
            for (int i = 0; i < feeConfigs; i++) {
                FeeConfig feeConfig = new FeeConfig(buffer);
                short accountsNumber = buffer.getShort();
                List<AccountId> accounts = new ArrayList<>();
                for (int j = 0; j < accountsNumber; j++) {
                    accounts.add(AccountId.fromBytes(buffer));
                }
                configs.put(feeConfig, accounts);
            }
        }
    }

    @Override
    public int size() {
        return 1 +
                (whitelistAll ? 1 : 0) +
                (rootConfig == null ? 0 : rootConfig.size()) +
                (whitelistAll ? 0 : 2 +
                        configs.entrySet()
                                .stream()
                                .mapToInt((e) ->
                                        e.getKey()
                                                .size() + 2 + e.getValue()
                                                .stream()
                                                .mapToInt(AccountId::size)
                                                .sum())
                                .sum());
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
                buffer.putShort((short) v.size());
                for (AccountId accountId : v) {
                    accountId.putBytes(buffer);
                }
            });
        }
    }
}
