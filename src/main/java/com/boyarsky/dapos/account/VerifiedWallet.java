/*
 * Copyright © 2018 Apollo Foundation
 */

package com.boyarsky.dapos.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedWallet {
    private Wallet wallet;
    private Status extractStatus;
}
