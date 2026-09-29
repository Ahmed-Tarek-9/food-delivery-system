package com.masrdelivery.exception;

import com.masrdelivery.domain.restaurant.MenuItem;

public class ItemUnavailableException extends PlatformException {
    public ItemUnavailableException(MenuItem menuItem) {
        super("Item " + menuItem.getName() + " is unavailable");
    }
}
