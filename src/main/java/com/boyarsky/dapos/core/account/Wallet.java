package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.utils.Convert;
import lombok.Data;

import java.security.KeyPair;
import java.util.Objects;

@Data
public class Wallet {
    private final AccountId account;
    private final KeyPair keyPair;

    public String getAppSpecificAccount() {
        return account.getAppSpecificAccount();
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "account='" + account + '\'' +
                ", publicKey=" + Convert.toHexString(keyPair.getPublic().getEncoded()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Objects.equals(account, wallet.account) &&
                Objects.equals(keyPair.getPrivate(), wallet.keyPair.getPrivate()) &&
                Objects.equals(keyPair.getPublic(), wallet.keyPair.getPublic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, keyPair.getPrivate(), keyPair.getPublic());
    }
}
