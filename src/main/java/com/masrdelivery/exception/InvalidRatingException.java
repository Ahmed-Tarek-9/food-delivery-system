package com.masrdelivery.exception;

import java.math.BigDecimal;

public class InvalidRatingException extends PlatformException {
    public InvalidRatingException(BigDecimal rating) {
        super("Invalid rating: " + rating);
    }
}
