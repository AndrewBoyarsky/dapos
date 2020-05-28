package com.boyarsky.dapos.web.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestError {
    private String errorMessage;
    private String stackTrace;
}
