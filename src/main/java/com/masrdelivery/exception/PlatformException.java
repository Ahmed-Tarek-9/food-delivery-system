package com.masrdelivery.exception;

public abstract class PlatformException extends RuntimeException {

    public PlatformException(String message) {
        super(message);
    }
}
