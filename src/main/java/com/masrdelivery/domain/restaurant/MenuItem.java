package com.masrdelivery.domain.restaurant;

import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public abstract class MenuItem {
    private String id;
    private String name;
    private MenuItemCategory category;
    private final int preparationTimeMinutes;
    private boolean isAvailable;
    private BigDecimal stock;

    protected MenuItem(String id, String name, MenuItemCategory category, int preparationTimeMinutes, boolean isAvailable, BigDecimal stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Menu Item ID cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidStringException("Menu Item name cannot be empty.");
        }
        if (category == null) {
            throw new NullEntityException("Menu Item category cannot be null.");
        }
        if (preparationTimeMinutes <= 0) {
            throw new IllegalArgumentException("Menu Item preparation time must be positive.");
        }
        if (stock == null) {
            throw new NullEntityException("Menu Item stock cannot be null.");
        }
        this.id = id;
        this.name = name;
        this.category = category;
        this.preparationTimeMinutes = preparationTimeMinutes;
        this.isAvailable = isAvailable;
        this.stock = stock;
    }

    public abstract BigDecimal calculatePrice(BigDecimal quantity);

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void markAvailable() {
        isAvailable = true;
    }

    public void markUnavailable() {
        isAvailable = false;
    }

    public MenuItemCategory getCategory() {
        return category;
    }

    public int getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void restock(BigDecimal quantity) {
        this.stock = this.stock.add(quantity);
    }

    public void deductStock(BigDecimal quantity) {
        this.stock = this.stock.subtract(quantity);
    }
}
