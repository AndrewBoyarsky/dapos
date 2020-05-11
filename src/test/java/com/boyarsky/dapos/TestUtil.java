package com.boyarsky.dapos;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.utils.Convert;

import java.util.Random;

public class TestUtil {
    public static Account generateEd25Acc() {
        Random random = new Random();
        Wallet wallet = CryptoUtils.generateEd25Wallet();
        Account acc = new Account(wallet.getAccount(), CryptoUtils.compress(wallet.getKeyPair().getPublic()), random.nextInt(1000), Account.Type.ORDINARY);
        acc.setHeight(random.nextInt(103));
        return acc;
    }

    public static byte[] generateBytes(int size) {
        Random random = new Random();
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String generateByteString(int size) {
        return Convert.toHexString(generateBytes(size));
    }
}
