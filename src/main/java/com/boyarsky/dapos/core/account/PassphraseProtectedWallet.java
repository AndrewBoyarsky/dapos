package com.boyarsky.dapos.core.account;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.security.Key;
import java.security.KeyPair;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PassphraseProtectedWallet extends Wallet {
    private String password;
    public PassphraseProtectedWallet(Wallet wallet) {
        super(wallet.getAccount(), wallet.getKeyPair());
    }

    public PassphraseProtectedWallet(String account, KeyPair keyPair, String password) {
        super(new AccountId(account), keyPair);
        this.password = password;
    }
}
