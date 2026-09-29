package com.masrdelivery.domain.common.id;

public class MenuItemIdGenerator implements IdGenerator {
    private static int nextId = 1;

    @Override
    public String generate() {
        return String.format("MI%04d", nextId++);
    }
}
