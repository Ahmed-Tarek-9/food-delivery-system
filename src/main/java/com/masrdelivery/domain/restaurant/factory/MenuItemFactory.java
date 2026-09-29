package com.masrdelivery.domain.restaurant.factory;

import com.masrdelivery.domain.common.id.MenuItemIdGenerator;
import com.masrdelivery.domain.restaurant.*;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.util.List;

public class MenuItemFactory {

    private final MenuItemIdGenerator menuItemIdGenerator;

    public MenuItemFactory(MenuItemIdGenerator menuItemIdGenerator) {
        if (menuItemIdGenerator == null)
            throw new NullEntityException("MenuItemIdGenerator");
        this.menuItemIdGenerator = menuItemIdGenerator;
    }

    public MenuItem create(MenuItemConfig config) {
        if (config == null || config.menuItemType() == null)
            throw new NullEntityException("MenuItemConfig");

        String id = menuItemIdGenerator.generate();

        return switch (config.menuItemType()) {
            case STANDARD -> new StandardItem(
                    id,
                    config.name(),
                    config.category(),
                    config.preparationTimeMinutes(),
                    config.isAvailable(),
                    config.stock(),
                    config.pricePer()
            );
            case WEIGHTED -> new WeightedItem(
                    id,
                    config.name(),
                    config.category(),
                    config.preparationTimeMinutes(),
                    config.isAvailable(),
                    config.stock(),
                    config.pricePer()
            );
            case COMBO -> new ComboItem(
                    id,
                    config.name(),
                    config.category(),
                    config.preparationTimeMinutes(),
                    config.isAvailable(),
                    config.stock(),
                    config.pricePer(),
                    config.bundle()
            );
        };
    }
}
