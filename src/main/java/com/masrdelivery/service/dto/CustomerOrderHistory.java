package com.masrdelivery.service.dto;

import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.util.List;

public record CustomerOrderHistory(
        Customer customer,
        List<Order> orders,
        BigDecimal totalSpent
) {
    public CustomerOrderHistory{
        if (customer == null) {
            throw new NullEntityException("Customer");
        }
    }
}
