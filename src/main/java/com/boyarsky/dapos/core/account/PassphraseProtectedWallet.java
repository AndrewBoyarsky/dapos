package com.boyarsky.dapos.core.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassphraseProtectedWallet extends Wallet {
    private String password;
    public PassphraseProtectedWallet(Wallet wallet) {
        super(wallet.getAccount(), wallet.getPublicKey(), wallet.getPrivateKey());
    }

    public PassphraseProtectedWallet(String account, byte[] publicKey, byte[] privateKey, String password) {
        super(new AccountId(account), publicKey, privateKey);
        this.password = password;
    }
}