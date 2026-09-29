package com.masrdelivery.domain.restaurant;

import com.masrdelivery.exception.InvalidPriceException;
import com.masrdelivery.exception.InvalidQuantityException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class WeightedItem extends MenuItem {
    private final BigDecimal pricePerKg;

    public WeightedItem(String id, String name, MenuItemCategory category, int preparationTimeMinutes,
                        boolean isAvailable, BigDecimal stock, BigDecimal pricePerKg) {
        super(id, name, category, preparationTimeMinutes, isAvailable, stock);
        if (pricePerKg == null)
            throw new NullEntityException("BigDecimal");
        if (pricePerKg.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidPriceException("Price per kg must be positive.");
        this.pricePerKg = pricePerKg;
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal weight) {
        if (weight == null)
            throw new NullEntityException("BigDecimal");
        if (weight.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidQuantityException(weight);
        return pricePerKg.multiply(weight);
    }
}
