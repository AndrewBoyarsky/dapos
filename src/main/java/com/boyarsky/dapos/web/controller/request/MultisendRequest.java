package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.web.validation.ValidAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class MultisendRequest extends DefaultSendingRequest {
    @NotEmpty
    private List<Transfer> transfers = new ArrayList<>();

    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @Null @ValidAccount AccountId getRecipient() {
        return super.getRecipient();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transfer {
        @NotNull
        @ValidAccount(allowedTypes = {"ETH", "BTC", "ED25"})
        private AccountId recipient;
        @NotNull
        @Positive
        private Long amount;
    }

}
