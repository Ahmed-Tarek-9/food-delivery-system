package com.masrdelivery.exception;

public class RestaurantClosedException extends PlatformException {
    public RestaurantClosedException() {
        super("Restaurant is closed");
    }
}
