package com.boyarsky.dapos.account;

import com.boyarsky.dapos.utils.Convert;
import lombok.Data;

@Data
public class Wallet {
    private final String account;
    private final byte[] publicKey;
    private final byte[] privateKey;

    @Override
    public String toString() {
        return "Wallet{" +
                "account='" + account + '\'' +
                ", publicKey=" + Convert.toHexString(publicKey) +
                '}';
    }

    public String getAppAccount() {
        if (isBitcoin()) {
            return "dab" + account;
        }
        if (isEth()) {
            return "det" + account;
        }
        throw new RuntimeException("Unknow wallet type");
    }

    public boolean isBitcoin() {
        return account.startsWith("1");
    }

    public boolean isEth() {
        return account.startsWith("0x");
    }
}
