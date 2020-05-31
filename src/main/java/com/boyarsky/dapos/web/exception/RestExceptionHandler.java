package com.boyarsky.dapos.web.exception;

import com.boyarsky.dapos.core.service.account.NotFoundException;
import com.boyarsky.dapos.web.ValidationUtil;
import com.boyarsky.dapos.web.controller.TransactionSendingException;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler {

    @SneakyThrows
    @ExceptionHandler(value
            = {NotFoundException.class})
    protected ResponseEntity<Object> handle(
            NotFoundException e, WebRequest request) {
        String bodyOfResponse = e.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestError(bodyOfResponse, ValidationUtil.dumpException(e)));
    }

    @SneakyThrows
    @ExceptionHandler(value
            = {RestValidationException.class})
    protected ResponseEntity<Object> handle(
            RestValidationException e, WebRequest request) {
        String bodyOfResponse = e.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestError(bodyOfResponse, ValidationUtil.dumpException(e)));
    }


    @SneakyThrows
    @ExceptionHandler(value = {TransactionSendingException.class})
    protected ResponseEntity<Object> handle(TransactionSendingException e) {
        String message = e.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestError(message, ValidationUtil.dumpException(e)));
    }

    @SneakyThrows
    @ExceptionHandler(value = {BindException.class})
    protected ResponseEntity<Object> handleException(BindException e, WebRequest request) {
        List<ObjectError> allErrors = e.getAllErrors();
        StringBuilder message = new StringBuilder();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError) {
                FieldError casted = (FieldError) error;
                message.append(error.getObjectName()).append(".").append(casted.getField()).append(" ").append(error.getDefaultMessage());
            } else {
                message.append(error.getObjectName()).append(" ").append(error.getDefaultMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestError(message.toString(), ValidationUtil.dumpException(e)));
    }
}