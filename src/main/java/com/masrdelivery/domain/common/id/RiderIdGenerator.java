package com.masrdelivery.domain.common.id;

public class RiderIdGenerator implements IdGenerator {
    private static int nextId = 1;

    @Override
    public String generate() {
        return String.format("RI%04d", nextId++);
    }
}
