package com.masrdelivery.exception;

public class DuplicateMenuItemException extends PlatformException {
    public DuplicateMenuItemException(String id) {
        super("Menu Item with ID: " + id + " already exists.");
    }
}
