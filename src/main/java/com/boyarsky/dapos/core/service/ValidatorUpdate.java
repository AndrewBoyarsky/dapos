package com.boyarsky.dapos.core.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidatorUpdate {
    private byte[] publicKey;
    private long power;
}
