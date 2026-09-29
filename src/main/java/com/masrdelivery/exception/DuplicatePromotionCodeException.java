package com.masrdelivery.exception;

public class DuplicatePromotionCodeException extends PlatformException {
    public DuplicatePromotionCodeException(String code) {
        super("Promotion Code: " + code + " already exists");
    }
}
