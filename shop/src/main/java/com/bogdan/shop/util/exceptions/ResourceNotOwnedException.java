package com.bogdan.shop.util.exceptions;

public class ResourceNotOwnedException extends RuntimeException {
    public ResourceNotOwnedException(String message) {
        super(message);
    }
}
