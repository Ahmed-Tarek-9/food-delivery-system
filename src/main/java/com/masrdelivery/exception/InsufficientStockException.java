package com.masrdelivery.exception;

public class InsufficientStockException extends PlatformException {
    public InsufficientStockException(String itemName) {
        super("Insufficient Stock for item: " + itemName);
    }
}
