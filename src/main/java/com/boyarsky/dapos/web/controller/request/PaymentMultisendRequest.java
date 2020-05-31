package com.boyarsky.dapos.web.controller.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class PaymentMultisendRequest extends MultisendRequest {
    @Override
    public @NotEmpty @Size(min = 2, max = 1000) List<Transfer> getTransfers() {
        return super.getTransfers();
    }
}
