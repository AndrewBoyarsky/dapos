package com.boyarsky.dapos.web.controller.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class MessageRequest extends DefaultSendingRequest {
    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @NotNull Boolean getIsToSelf() {
        return super.getIsToSelf();
    }

    @Override
    public @NotEmpty String getMessage() {
        return super.getMessage();
    }
}
