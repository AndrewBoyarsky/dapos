package com.boyarsky.dapos.web.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CurrencyMultisendRequest extends MultisendRequest {
    @NotNull
    private Long currencyId;

    @Override
    public @NotEmpty @Size(min = 2, max = 800) List<Transfer> getTransfers() {
        return super.getTransfers();
    }
}
