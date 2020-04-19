package com.boyarsky.dapos.web.controller.exception;

import com.boyarsky.dapos.core.service.account.NotFoundException;
import com.boyarsky.dapos.web.controller.TransactionSendingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = {NotFoundException.class})
    protected ResponseEntity<Object> handle(
            NotFoundException e, WebRequest request) {
        String bodyOfResponse = e.getMessage();
        return handleExceptionInternal(e, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {TransactionSendingException.class})
    protected ResponseEntity<Object> handle(TransactionSendingException e, WebRequest request) {
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}