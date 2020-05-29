package com.boyarsky.dapos.web.controller.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public class PaymentRequest extends DefaultSendingRequest {
    @Override
    public @NotNull @PositiveOrZero Long getAmount() {
        return super.getAmount();
    }
}
