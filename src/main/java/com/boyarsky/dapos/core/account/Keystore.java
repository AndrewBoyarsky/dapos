package com.boyarsky.dapos.core.account;

public interface Keystore {

    Wallet createBitcoin(String pass);

    Wallet createEthereum(String pass);

    Wallet createEd25(String pass);

    VerifiedWallet getWallet(String account, String pass);
}
