package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderPriorityComparator;
import com.masrdelivery.exception.NullEntityException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class OrderDispatchQueue {

    private final PriorityQueue<Order> readyQueue;

    public OrderDispatchQueue() {
        this.readyQueue = new PriorityQueue<>(new OrderPriorityComparator());
    }

    public synchronized void enqueueReadyOrder(Order order) {
        if (order == null) {
            throw new NullEntityException("Order");
        }
        readyQueue.offer(order);
    }

    public synchronized Optional<Order> pollNextOrder() {
        return Optional.ofNullable(readyQueue.poll());
    }

    public synchronized Optional<Order> pollNextForDistrict(District district) {
        if (district == null || readyQueue.isEmpty()) {
            return Optional.empty();
        }

        List<Order> skipped = new ArrayList<>();
        Order matchedOrder = null;

        while (!readyQueue.isEmpty()) {
            Order current = readyQueue.poll();
            if (current.getRestaurant().getDistrict().equals(district)) {
                matchedOrder = current;
                break;
            }
            skipped.add(current);
        }

        readyQueue.addAll(skipped);

        return Optional.ofNullable(matchedOrder);
    }

    public synchronized boolean isEmpty() {
        return readyQueue.isEmpty();
    }

    public synchronized int size() {
        return readyQueue.size();
    }
}
