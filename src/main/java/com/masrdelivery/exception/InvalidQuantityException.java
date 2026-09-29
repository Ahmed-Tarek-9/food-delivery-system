package com.masrdelivery.exception;

import java.math.BigDecimal;

public class InvalidQuantityException extends PlatformException {
    public InvalidQuantityException(BigDecimal quantity) {
        super("Invalid quantity: " + quantity);
    }
}
