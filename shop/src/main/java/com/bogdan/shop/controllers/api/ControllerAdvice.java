package com.bogdan.shop.controllers.api;

import com.bogdan.shop.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.exceptions.ResourceNotOwnedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(value = {ResourceDoesNotExistException.class, ResourceNotOwnedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleExceptions(Exception ex) {
        return ex.getMessage();
    }
}
