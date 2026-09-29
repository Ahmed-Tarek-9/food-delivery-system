package com.masrdelivery.domain.common.id;

public class RestaurantIdGenerator implements IdGenerator {
    private static int nextId = 1;

    @Override
    public String generate() {
        return String.format("RE%04d", nextId++);
    }
}
