package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.web.validation.ValidAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class FeeProviderRequest extends DefaultSendingRequest {
    @NotNull
    private PartyFeeConfigJson fromConfig;
    @NotNull
    private PartyFeeConfigJson toFeeConfig;

    @Override
    public @Positive @NotNull Long getAmount() {
        return super.getAmount();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyFeeConfigJson {
        FeeConfigJson rootConfig;
        List<FeeConfigGroup> configGroups;

        public boolean whitelistAll() {
            return configGroups == null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeConfigJson {
        @Positive
        private int operations = -1;
        @Positive
        private long maxAllowedFee = -1;
        @Positive
        private long totalAllowedFee = -1;

        private Set<TxType> types = new HashSet<>();
    }

    @Data
    public static class FeeConfigGroup {
        @NotNull
        private FeeConfigJson groupConfig;
        @NotEmpty
        private List<@ValidAccount AccountId> accounts;
    }
}
