package com.masrdelivery.exception;

public class DuplicateConditionException extends PlatformException {
    public DuplicateConditionException(String type) {
        super("Duplicate condition of type: " + type);
    }
}
