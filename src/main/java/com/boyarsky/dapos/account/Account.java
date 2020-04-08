package com.boyarsky.dapos.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private long dbId;
    private String cryptoId;
    private byte[] publicKey;
    private long balance;
    private long height;
    public enum Type {
        CONTRACT,ORDINARY, FEE_PROVIDER, MULTISIG
    }
}
