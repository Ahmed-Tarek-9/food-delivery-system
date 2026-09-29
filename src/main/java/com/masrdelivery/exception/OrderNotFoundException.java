package com.masrdelivery.exception;

public class OrderNotFoundException extends PlatformException {
    public OrderNotFoundException(String orderId) {
        super("Order with ID: " + orderId + " doesn't exist.");
    }
}
