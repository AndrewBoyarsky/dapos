package com.boyarsky.dapos.web.controller.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public class PaymentRequest extends DefaultSendingRequest {
    @Override
    public void setAmount(@NotNull @PositiveOrZero Long amount) {
        super.setAmount(amount);
    }
}
