package com.bogdan.shop.controllers.api;

import com.bogdan.shop.util.exceptions.OperationNotSupportedException;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ResourceNotOwnedException.class, OperationNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleExceptions(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(value = {ResourceDoesNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(Exception ex) {
        return ex.getMessage();
    }

}