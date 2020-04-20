package com.boyarsky.dapos.core.model.fee;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.ByteSerializable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class PartyFeeConfig implements ByteSerializable {
    private boolean whitelistAll;
    private FeeConfig rootConfig;
    private Map<FeeConfig, List<AccountId>> configs = new LinkedHashMap<>();

    public PartyFeeConfig(boolean whitelistAll, FeeConfig rootConfig, Map<FeeConfig, List<AccountId>> configs) {
        this.whitelistAll = whitelistAll;
        this.rootConfig = rootConfig;
        if (configs != null) {
            this.configs.putAll(configs);
        }
    }

    public Optional<FeeConfig> forAccount(AccountId accountId) {
        if (whitelistAll) {
            return Optional.ofNullable(rootConfig);
        }
        return this.configs.entrySet().stream().filter(e -> e.getValue().contains(accountId)).map(Map.Entry::getKey).findAny();

    }

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

        int size = 1;
        if (whitelistAll) {
            size++;
            if (rootConfig != null) {
                size += rootConfig.size();
            }
        } else {
            size += 2;
            int computedSize = configs.entrySet()
                    .stream()
                    .mapToInt((e) -> e.getKey()
                            .size() + 2 + e.getValue()
                            .stream()
                            .mapToInt(AccountId::size)
                            .sum())
                    .sum();
            size += computedSize;
        }


        return size;
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
