package com.boyarsky.dapos.core.model.message;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MessageEntity extends BlockchainEntity {
    private long mid;
    private EncryptedData data;
    private AccountId sender;
    private AccountId recipient;
    private boolean compressed;


    public boolean isToSelf() {
        return recipient == null;
    }
}
