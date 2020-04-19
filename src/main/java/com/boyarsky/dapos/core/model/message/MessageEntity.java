package com.boyarsky.dapos.core.model.message;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
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
