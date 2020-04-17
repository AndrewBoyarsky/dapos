package com.boyarsky.dapos.core.service.keystore;

import com.boyarsky.dapos.core.model.keystore.VerifiedWallet;
import com.boyarsky.dapos.core.model.keystore.Wallet;

public interface Keystore {

    Wallet createBitcoin(String pass);

    Wallet createEthereum(String pass);

    Wallet createEd25(String pass);

    VerifiedWallet getWallet(String account, String pass);
}
