package com.boyarsky.dapos.web.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@EqualsAndHashCode(callSuper = true)
@Data
public class ValidatorToggleRequest extends DefaultSendingRequest {
    @NotNull
    private Boolean enable;

    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }
}
