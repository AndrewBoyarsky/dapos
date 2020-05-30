package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.validation.ValidAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
public class CurrencyIssueRequest extends DefaultSendingRequest {
    @NotEmpty
    @Size(min = 3, max = 8)
    private String code;
    @NotEmpty
    @Size(min = 5, max = 20)
    private String name;
    @Size(max = 100)
    private String description;
    @Positive
    private long supply;
    @PositiveOrZero
    private byte decimals;

    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @Null @ValidAccount AccountId getRecipient() {
        return super.getRecipient();
    }
}
