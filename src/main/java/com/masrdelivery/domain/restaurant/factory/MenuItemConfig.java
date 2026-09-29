package com.masrdelivery.domain.restaurant.factory;

import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.MenuItemCategory;
import com.masrdelivery.domain.restaurant.common.MenuItemType;
import com.masrdelivery.exception.NullEntityException;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public record MenuItemConfig(
        String name,
        MenuItemCategory category,
        int preparationTimeMinutes,
        boolean isAvailable,
        BigDecimal stock,
        BigDecimal pricePer,
        MenuItemType menuItemType,
        List<MenuItem> bundle
) {
    public MenuItemConfig {
        if (menuItemType == null)
            throw new NullEntityException("MenuItemType");
    }

    public static MenuItemConfig standard(
            String name,
            MenuItemCategory category,
            int preparationTimeMinutes,
            boolean isAvailable,
            BigDecimal stock,
            BigDecimal pricePer
    ) {
        return new MenuItemConfig(
                name,
                category,
                preparationTimeMinutes,
                isAvailable,
                stock,
                pricePer,
                MenuItemType.STANDARD,
                null);
    }

    public static MenuItemConfig weighted(
            String name,
            MenuItemCategory category,
            int preparationTimeMinutes,
            boolean isAvailable,
            BigDecimal stock,
            BigDecimal pricePer
    ) {
        return new MenuItemConfig(
                name,
                category,
                preparationTimeMinutes,
                isAvailable,
                stock,
                pricePer,
                MenuItemType.WEIGHTED,
                null);
    }

    public static MenuItemConfig combo(
            String name,
            MenuItemCategory category,
            int preparationTimeMinutes,
            boolean isAvailable,
            BigDecimal stock,
            BigDecimal pricePer,
            List<MenuItem> bundle
    ) {
        return new MenuItemConfig(
                name,
                category,
                preparationTimeMinutes,
                isAvailable,
                stock,
                pricePer,
                MenuItemType.COMBO,
                List.copyOf(bundle));
    }
}
