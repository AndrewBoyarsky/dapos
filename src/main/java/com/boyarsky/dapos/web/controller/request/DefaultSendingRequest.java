package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.validation.ValidAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultSendingRequest extends Credentials {

    @ValidAccount
    @Schema(implementation = String.class)
    private AccountId recipient;


    private Long amount;
    private Long feeProvider;

    private MessageData messageData;

    @Data
    public static class MessageData {
        @NotEmpty
        String message;
        @NotNull
        Boolean isToSelf;
    }

}
