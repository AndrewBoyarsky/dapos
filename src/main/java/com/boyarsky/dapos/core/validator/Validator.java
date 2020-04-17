package com.boyarsky.dapos.core.validator;

import com.boyarsky.dapos.core.model.account.Account;

public class Validator {
    private Account validatorAccount;
    private boolean enabled;
    private int fee; // percents multiplied by 100 to support fractions like 10.52 or 0.01
}
