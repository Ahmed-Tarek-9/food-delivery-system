package com.masrdelivery.exception;

public class RiderNotFoundException extends PlatformException {
    public RiderNotFoundException(String id) {
        super("Rider with ID: " + id + " does not exist.");
    }
}
