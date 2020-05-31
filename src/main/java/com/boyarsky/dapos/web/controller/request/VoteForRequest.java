package com.boyarsky.dapos.web.controller.request;

import org.checkerframework.checker.index.qual.Positive;

import javax.validation.constraints.NotNull;

public class VoteForRequest extends DefaultSendingRequest {
    @Override
    public @NotNull @Positive Long getAmount() {
        return super.getAmount();
    }
}
