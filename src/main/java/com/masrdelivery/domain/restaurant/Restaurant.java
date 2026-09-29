package com.masrdelivery.domain.restaurant;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.exception.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Restaurant {
    private final String id;
    private String name;
    private District district;
    private Set<Cuisine> cuisines;
    private BigDecimal rating;
    private boolean isOpen;
    private final Map<String, MenuItem> menu;

    private Restaurant(Builder builder) {
        this.id = builder.id;
        if (builder.name == null || builder.name.isBlank()) {
            throw new InvalidStringException("Restaurant name cannot be empty.");
        }
        if (builder.rating.compareTo(BigDecimal.ZERO) < 0 || builder.rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new InvalidRatingException(builder.rating);
        }
        if (builder.district == null) {
            throw new NullEntityException("District");
        }
        this.name = builder.name;
        this.district = builder.district;
        this.cuisines = builder.cuisines != null ? new HashSet<>(builder.cuisines) : new HashSet<>();
        this.rating = builder.rating;
        this.isOpen = true;
        menu = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public Set<Cuisine> getCuisines() {
        return cuisines;
    }

    public void addCuisine(Cuisine cuisine) {
        if (cuisine == null) {
            throw new NullEntityException("Cuisine");
        }
        this.cuisines.add(cuisine);
    }

    public void removeCuisine(Cuisine cuisine) {
        if (cuisine == null) {
            throw new NullEntityException("Cuisine");
        }
        if (!this.cuisines.remove(cuisine)) {
            throw new CuisineNotFoundException("Cannot remove a cuisine that doesn't exist.");
        }
    }

    public District getDistrict() {
        return district;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Map<String, MenuItem> getMenu() {
        return menu;
    }

    public MenuItem findMenuItem(String menuItemId) {
        MenuItem menuItem = menu.get(menuItemId);
        if (menuItem == null) {
            throw new MenuItemNotFoundException("Cannot find menu item with ID: " + menuItemId);
        }
        return menuItem;
    }

    public void addMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new NullEntityException("Menu Item");
        }
        if (this.menu.containsKey(menuItem.getId())) {
            throw new DuplicateMenuItemException(menuItem.getId());
        }
        this.menu.put(menuItem.getId(), menuItem);
    }

    public void removeMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new NullEntityException("Menu Item");
        }
        if (this.menu.containsKey(menuItem.getId())) {
            this.menu.remove(menuItem.getId());
        } else {
            throw new MenuItemNotFoundException("Cannot remove a menu item that doesn't exist.");
        }
    }

    public void markMenuItemAvailable(MenuItem menuItem) {
        if (menuItem == null) {
            throw new NullEntityException("Menu Item");
        }
        if (menu.containsKey(menuItem.getId())) {
            menuItem.markAvailable();
        } else {
            throw new MenuItemNotFoundException("Cannot make menu item available as it doesn't exist within the restaurant.");
        }
    }

    public void markMenuItemUnavailable(MenuItem menuItem) {
        if (menuItem == null) {
            throw new NullEntityException("Menu Item");
        }
        if (menu.containsKey(menuItem.getId())) {
            menuItem.markUnavailable();
        } else {
            throw new MenuItemNotFoundException("Cannot make menu item unavailable as it doesn't exist within the restaurant.");
        }
    }

    public void markRestaurantOpen() {
        this.isOpen = true;
    }

    public void markRestaurantClosed() {
        this.isOpen = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant restaurant)) return false;
        return Objects.equals(id, restaurant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private String id;
        private String name;
        private District district;
        private Set<Cuisine> cuisines = new HashSet<Cuisine>();
        private BigDecimal rating;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder district(District district) {
            this.district = district;
            return this;
        }

        public Builder cuisine(Cuisine cuisine) {
            if (cuisine != null) {
                cuisines.add(cuisine);
            }
            return this;
        }

        public Builder cuisines(Collection<Cuisine> cuisines) {
            this.cuisines = new HashSet<>(cuisines);
            return this;
        }

        public Builder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
