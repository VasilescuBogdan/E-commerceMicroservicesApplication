package com.bogdan.shop.exceptions;

public class ResourceNotOwnedException extends RuntimeException {
    public ResourceNotOwnedException(String message) {
        super(message);
    }
}
