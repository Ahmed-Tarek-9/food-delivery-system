package com.masrdelivery.exception;

public class CustomerNotFoundException extends PlatformException {
    public CustomerNotFoundException(String id) {
        super("Customer with ID: " + id + " does not exist.");
    }
}
