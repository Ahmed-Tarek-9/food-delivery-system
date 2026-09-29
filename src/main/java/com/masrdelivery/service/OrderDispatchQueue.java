package com.masrdelivery.service;

import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderPriorityComparator;
import com.masrdelivery.exception.NullEntityException;

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

    public synchronized boolean isEmpty() {
        return readyQueue.isEmpty();
    }

    public synchronized int size() {
        return readyQueue.size();
    }
}
