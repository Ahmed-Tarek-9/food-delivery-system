package com.masrdelivery.exception;

public class RestaurantNotFoundException extends PlatformException {
    public RestaurantNotFoundException(String id) {
        super("Restaurant with ID: " + id + " does not exist.");
    }
}
