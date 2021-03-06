package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.validation.ValidAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@EqualsAndHashCode(callSuper = true)
@Data
public class CurrencyTransferRequest extends DefaultSendingRequest {
    @NotNull
    private Long currencyId;

    @Override
    public @Positive Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @NotNull @ValidAccount AccountId getRecipient() {
        return super.getRecipient();
    }
}
