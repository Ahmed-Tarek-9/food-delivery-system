package com.masrdelivery.domain.common.id;

public class OrderIdGenerator implements IdGenerator {
    private static int nextId = 1;

    @Override
    public String generate() {
        return String.format("OR%04d", nextId++);
    }
}
