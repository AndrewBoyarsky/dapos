package com.boyarsky.dapos.web.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
public class SendMessageRequest extends DefaultSendingRequest {
    @NotBlank
    String data;
    boolean isToSelf;
}
