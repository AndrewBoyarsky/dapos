package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.controller.validation.ValidAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.PositiveOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultSendingRequest extends Credentials {

    @ValidAccount
    @Schema(implementation = String.class)
    private AccountId recipient;
    @PositiveOrZero
    private long amount = 0;

    private long feeProvider = 0;

    String data;
    boolean isToSelf;
}
