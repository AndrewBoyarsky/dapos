package com.boyarsky.dapos.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedData {
    private byte[] encrypted;
    private byte[] nonce;
}
