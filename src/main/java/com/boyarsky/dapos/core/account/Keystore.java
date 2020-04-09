package com.boyarsky.dapos.core.account;

public interface Keystore {

    Wallet createBitcoin(String pass);

    Wallet createEthereum(String pass);
}
