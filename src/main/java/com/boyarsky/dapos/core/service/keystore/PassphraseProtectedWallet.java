package com.boyarsky.dapos.core.service.keystore;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
