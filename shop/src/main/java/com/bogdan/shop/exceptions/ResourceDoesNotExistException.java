package com.bogdan.shop.exceptions;

public class ResourceDoesNotExistException extends RuntimeException {
    public ResourceDoesNotExistException(String message) {
        super(message);
    }
}
