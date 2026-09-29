package com.masrdelivery.domain.restaurant;

import com.masrdelivery.exception.EmptyCollectionException;
import com.masrdelivery.exception.InvalidPriceException;
import com.masrdelivery.exception.InvalidQuantityException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ComboItem extends MenuItem {
    private final List<MenuItem> bundledItems;
    private final BigDecimal comboPrice;

    public ComboItem(String id, String name, MenuItemCategory category, int preparationTimeMinutes,
                     boolean isAvailable, BigDecimal stock, BigDecimal comboPrice, List<MenuItem> bundledItems) {
        super(id, name, category, preparationTimeMinutes, isAvailable, stock);
        if (bundledItems == null)
            throw new NullEntityException("List<MenuItem>");
        if (comboPrice == null)
            throw new NullEntityException("BigDecimal");
        if (comboPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidPriceException("Combo price must be positive.");
        if (bundledItems.isEmpty())
            throw new EmptyCollectionException("Combo must have at least one bundled item.");
        if (bundledItems.stream().anyMatch(Objects::isNull))
            throw new NullEntityException("List<MenuItem>");
        this.bundledItems = List.copyOf(bundledItems);
        this.comboPrice = comboPrice;
    }

    public List<MenuItem> getBundledItems() {
        return List.copyOf(bundledItems);
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantity) {
        if (quantity == null)
            throw new NullEntityException("BigDecimal");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidQuantityException(quantity);
        return comboPrice.multiply(quantity);
    }
}
