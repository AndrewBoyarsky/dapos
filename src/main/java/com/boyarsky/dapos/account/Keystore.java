package com.boyarsky.dapos.account;

public interface Keystore {

    Wallet createBitcoin(String pass);

    Wallet createEthereum(String pass);
}
