package com.masrdelivery.exception;

public class PromotionNotFoundException extends PlatformException {
    public PromotionNotFoundException(String code) {
        super("Promotion Code: " + code + " doesn't exist.");
    }
}
