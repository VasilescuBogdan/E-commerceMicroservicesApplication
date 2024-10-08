package com.bogdan.order.controller.api;

import com.bogdan.order.utils.exception.ResourceDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ResourceDoesNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleExceptions(Exception ex) {
        return ex.getMessage();
    }

}
