package com.masrdelivery.exception;

public class InvalidWithdrawalAmountException extends PlatformException {
    public InvalidWithdrawalAmountException(String message) {
        super(message);
    }
}
