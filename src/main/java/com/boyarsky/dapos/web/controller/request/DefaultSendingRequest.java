package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.validation.ValidAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultSendingRequest extends Credentials {

    @ValidAccount
    @Schema(implementation = String.class)
    private AccountId recipient;

    String message;
    Boolean isToSelf;
    private Long amount;
    private Long feeProvider;
}
