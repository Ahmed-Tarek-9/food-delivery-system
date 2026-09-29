package com.masrdelivery.repository;

import com.masrdelivery.domain.order.Order;
import com.masrdelivery.exception.DuplicateEntityException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.OrderNotFoundException;

import java.util.*;

public class OrderRepository implements Repository<String, Order> {

    private final Map<String, Order> orders;

    public OrderRepository() {
        this.orders = new HashMap<>();
    }

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new NullEntityException("Order");
        }

        if (orders.containsKey(order.getId())) {
            throw new DuplicateEntityException("Order");
        }

        orders.put(order.getId(), order);
    }

    @Override
    public void update(Order order) {
        if (order == null) {
            throw new NullEntityException("Order");
        }

        if (!orders.containsKey(order.getId())) {
            throw new OrderNotFoundException(order.getId());
        }
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Order ID cannot be null or empty");
        }
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public Collection<Order> findAll() {
        return List.copyOf(orders.values());
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Order ID cannot be null or empty");
        }
        orders.remove(id);
    }
}
