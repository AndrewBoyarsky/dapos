/*
 * Copyright © 2018 Apollo Foundation
 */

package com.boyarsky.dapos.core.model.keystore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifiedWallet {
    private Wallet wallet;
    private Status extractStatus;
}
