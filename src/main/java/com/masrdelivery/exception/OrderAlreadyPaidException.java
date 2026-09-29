package com.masrdelivery.exception;

public class OrderAlreadyPaidException extends PlatformException {
    public OrderAlreadyPaidException(String orderId) {
        super("Order with ID: " + orderId + " is already paid");
    }
}
