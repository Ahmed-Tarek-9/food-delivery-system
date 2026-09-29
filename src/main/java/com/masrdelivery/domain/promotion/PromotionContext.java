package com.masrdelivery.domain.promotion;

import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class PromotionContext {

    private final Order order;

    public PromotionContext(Order order) {
        if (order == null)
            throw new NullEntityException("PromotionContext order");
        this.order = order;
    }

    public Customer getCustomer() {
        return order.getCustomer();
    }

    public Order getOrder() {
        return order;
    }

    public Restaurant getRestaurant() {
        return order.getRestaurant();
    }
}
