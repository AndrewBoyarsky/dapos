package com.boyarsky.dapos;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.keystore.Wallet;

import java.util.Random;

public class AccountUtil {
    public static Account generateAcc() {
        Random random = new Random();
        Wallet wallet = CryptoUtils.generateEd25Wallet();
        Account acc = new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), random.nextInt(1000), Account.Type.ORDINARY);
        acc.setHeight(random.nextInt(103));
        return acc;
    }
}
