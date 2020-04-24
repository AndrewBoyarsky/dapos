package com.boyarsky.dapos.web.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class CreateAccRequest {
    private String pass;
    @Max(4)
    @Min(1)
    private Integer type = 3;
}
