package com.bogdan.shop.util.exceptions;

public class OperationNotSupportedException extends RuntimeException {
    public OperationNotSupportedException() {
        super("Don't have permission to modify this resource!");
    }
}
