package com.boyarsky.dapos.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Data
public class SendMoneyRequest {
    @NotBlank
    private String account;
    @NotBlank
    private String pass;
    private String recipient;
    @PositiveOrZero
    private long amount = 0;
}
