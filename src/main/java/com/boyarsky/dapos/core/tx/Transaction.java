package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.account.Account;

public class Transaction {
    private String rawTransaction;
    private long txId;
    private Account sender;
    private Account recipient;
    private byte[] data;
    private long amount;
    private byte[] signature;

}
