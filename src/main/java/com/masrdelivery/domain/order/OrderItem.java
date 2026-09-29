package com.masrdelivery.domain.order;

import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class OrderItem {
    private final MenuItem menuItem;
    private final BigDecimal quantity;

    public OrderItem(MenuItem menuItem, BigDecimal quantity) {
        if (menuItem == null) {
            throw new NullEntityException("MenuItem");
        }
        if (quantity == null) {
            throw new NullEntityException("Quantity");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal calculateLineTotal() {
        return menuItem.calculatePrice(quantity);
    }
}
