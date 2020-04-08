package com.boyarsky.dapos.account;

import com.boyarsky.dapos.utils.Convert;
import lombok.Data;

@Data
public class Wallet {
    private final AccountId account;
    private final byte[] publicKey;
    private final byte[] privateKey;

    public String getAppSpecificAccount() {
        return account.getAppSpecificAccount();
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "account='" + account + '\'' +
                ", publicKey=" + Convert.toHexString(publicKey) +
                '}';
    }
}
