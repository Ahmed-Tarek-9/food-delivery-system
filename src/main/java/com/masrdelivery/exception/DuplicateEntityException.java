package com.masrdelivery.exception;

public class DuplicateEntityException extends PlatformException {
    public DuplicateEntityException(String type) {
        super("Duplicate entity of type: " + type);
    }
}
