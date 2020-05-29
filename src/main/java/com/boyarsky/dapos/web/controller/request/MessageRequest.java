package com.boyarsky.dapos.web.controller.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class MessageRequest extends DefaultSendingRequest {
    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @NotNull MessageData getMessageData() {
        return super.getMessageData();
    }
}
