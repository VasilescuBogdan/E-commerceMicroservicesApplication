package com.bogdan.order.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ResourceDoesNotExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleExceptions(Exception ex) {
        return ex.getMessage();
    }
}
