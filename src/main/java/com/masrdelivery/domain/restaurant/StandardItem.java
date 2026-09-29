package com.masrdelivery.domain.restaurant;

import com.masrdelivery.exception.InvalidPriceException;
import com.masrdelivery.exception.InvalidQuantityException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class StandardItem extends MenuItem {
    private final BigDecimal price;

    public StandardItem(String id, String name, MenuItemCategory category, int preparationTimeMinutes,
                        boolean isAvailable, BigDecimal stock, BigDecimal price) {
        super(id, name, category, preparationTimeMinutes, isAvailable, stock);
        if (price == null)
            throw new NullEntityException("BigDecimal");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidPriceException("Price must be positive.");
        this.price = price;
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantity) {
        if (quantity == null)
            throw new NullEntityException("BigDecimal");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidQuantityException(quantity);
        return price.multiply(quantity);
    }
}
