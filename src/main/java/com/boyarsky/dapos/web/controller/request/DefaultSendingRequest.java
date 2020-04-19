package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.controller.validation.ValidAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.PositiveOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultSendingRequest extends Credentials {

    @ValidAccount
    private AccountId recipient;
    @PositiveOrZero
    private long amount = 0;
}
