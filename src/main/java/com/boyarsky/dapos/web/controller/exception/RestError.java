package com.boyarsky.dapos.web.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestError {
    private String errorMessage;
    private String stackTrace;


}
