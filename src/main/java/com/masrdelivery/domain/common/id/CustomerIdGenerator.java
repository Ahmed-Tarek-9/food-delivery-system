package com.masrdelivery.domain.common.id;

public class CustomerIdGenerator implements IdGenerator {
    private static int nextId = 1;

    @Override
    public String generate() {
        return String.format("CU%04d", nextId++);
    }
}
