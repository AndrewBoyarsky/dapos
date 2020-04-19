package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.controller.validation.ValidAccount;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Credentials {
    @ValidAccount
    @NotNull
    private AccountId account;

    @NotBlank
    private String pass;
}
