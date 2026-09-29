package com.masrdelivery.exception;

public class NullEntityException extends PlatformException {
    public NullEntityException(String type) {
        super("Null entity of type: " + type);
    }
}
