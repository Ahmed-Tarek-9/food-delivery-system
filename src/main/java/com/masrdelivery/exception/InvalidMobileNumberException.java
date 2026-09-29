package com.masrdelivery.exception;

public class InvalidMobileNumberException extends PlatformException {
    public InvalidMobileNumberException(String mobileNumber) {
        super("Invalid mobile number: " + mobileNumber);
    }
}
