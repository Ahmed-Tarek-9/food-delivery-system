package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.restaurant.Cuisine;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.MenuItemCategory;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.restaurant.common.MenuItemType;
import com.masrdelivery.domain.restaurant.factory.MenuItemConfig;
import com.masrdelivery.domain.restaurant.factory.MenuItemFactory;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.RestaurantNotFoundException;
import com.masrdelivery.repository.RestaurantRepository;
import com.masrdelivery.domain.common.id.RestaurantIdGenerator;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantIdGenerator restaurantIdGenerator;
    private final MenuItemFactory menuItemFactory;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            RestaurantIdGenerator restaurantIdGenerator,
            MenuItemFactory menuItemFactory
    ) {
        if (restaurantIdGenerator == null)
            throw new NullEntityException("RestaurantIdGenerator");
        if (restaurantRepository == null)
            throw new NullEntityException("RestaurantRepository");
        if (menuItemFactory == null)
            throw new NullEntityException("MenuItemFactory");

        this.restaurantRepository = restaurantRepository;
        this.restaurantIdGenerator = restaurantIdGenerator;
        this.menuItemFactory = menuItemFactory;
    }

    public Restaurant createRestaurant(String name, District district, Collection<Cuisine> cuisines, BigDecimal rating) {
        Restaurant restaurant = new Restaurant.Builder()
                .id(restaurantIdGenerator.generate())
                .name(name)
                .district(district)
                .cuisines(new HashSet<>(cuisines))
                .rating(rating)
                .build();

        restaurantRepository.save(restaurant);

        return restaurant;
    }

    public void addCuisine(String restaurantId, Cuisine cuisine) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.addCuisine(cuisine);
    }

    public void removeCuisine(String restaurantId, Cuisine cuisine) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.removeCuisine(cuisine);
    }

    public MenuItem createMenuItem(
            String restaurantId,
            MenuItemType type,
            String name,
            MenuItemCategory category,
            int preparationTimeInMinutes,
            boolean isAvailable,
            BigDecimal stock,
            BigDecimal pricePer,
            List<MenuItem> bundle
    ) {
        Restaurant restaurant = findRestaurant(restaurantId);

        MenuItemConfig config = switch (type) {
            case STANDARD -> MenuItemConfig.standard(
                    name,
                    category,
                    preparationTimeInMinutes,
                    isAvailable,
                    stock,
                    pricePer
            );
            case WEIGHTED -> MenuItemConfig.weighted(
                    name,
                    category,
                    preparationTimeInMinutes,
                    isAvailable,
                    stock,
                    pricePer
            );
            case COMBO -> MenuItemConfig.combo(
                    name,
                    category,
                    preparationTimeInMinutes,
                    isAvailable,
                    stock,
                    pricePer,
                    bundle
            );
        };

        MenuItem menuItem = menuItemFactory.create(config);

        restaurant.addMenuItem(menuItem);

        return menuItem;
    }

    public void addMenuItem(String restaurantId, MenuItem menuItem) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.addMenuItem(menuItem);
    }

    public void removeMenuItem(String restaurantId, MenuItem menuItem) {
        Restaurant restaurant = findRestaurant(restaurantId);
        restaurant.removeMenuItem(menuItem);
    }

    public void updateMenuItemAvailability(String restaurantId, String menuItemId, boolean available) {
        Restaurant restaurant = findRestaurant(restaurantId);
        MenuItem menuItem = restaurant.findMenuItem(menuItemId);
        if (available) {
            restaurant.markMenuItemAvailable(menuItem);
        } else {
            restaurant.markMenuItemUnavailable(menuItem);
        }
    }

    public void updateRestaurantStatus(String restaurantId, boolean open) {
        Restaurant restaurant = findRestaurant(restaurantId);
        if (open) {
            restaurant.markRestaurantOpen();
        } else {
            restaurant.markRestaurantClosed();
        }
    }

    public Restaurant findRestaurant(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new InvalidStringException("Restaurant ID cannot be null or blank");
        }

        return restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new RestaurantNotFoundException(restaurantId)
        );
    }

    public List<Restaurant> listRestaurants() {
        return new ArrayList<>(restaurantRepository.findAll());
    }

    public List<Restaurant> searchRestaurants(RestaurantFilterCriteria criteria) {
        if (criteria == null) {
            return listRestaurants();
        }

        Predicate<Restaurant> predicate = restaurant -> true;

        if (criteria.district() != null) {
            predicate = predicate.and(restaurant -> restaurant.getDistrict() == criteria.district());
        }

        if (criteria.cuisine() != null) {
            predicate = predicate.and(restaurant -> restaurant.getCuisines().contains(criteria.cuisine()));
        }

        if (criteria.minRating() != null) {
            predicate = predicate.and(restaurant -> restaurant.getRating().compareTo(criteria.minRating()) >= 0);
        }

        if (criteria.maxPriceCieling() != null) {
            predicate = predicate.and(
                    restaurant -> restaurant
                            .getMenu()
                            .values()
                            .stream()
                            .anyMatch(
                                    item ->
                                            item.calculatePrice(BigDecimal.ONE).compareTo(criteria.maxPriceCieling()) <= 0
                            )
            );
        }

        return restaurantRepository
                .findAll()
                .stream()
                .filter(predicate)
                .sorted(
                        Comparator.comparing(Restaurant::getRating)
                                .reversed()
                                .thenComparing(Restaurant::getName)
                )
                .collect(Collectors.toList());
    }

    public List<Restaurant> findRestaurantsSortedByRating() {
        return restaurantRepository
                .findAll()
                .stream()
                .sorted(
                        Comparator
                                .comparing(Restaurant::getRating)
                                .reversed()
                                .thenComparing(Restaurant::getName)
                )
                .collect(Collectors.toList());
    }

    public Set<Cuisine> listCuisines() {
        return restaurantRepository
                .findAll()
                .stream()
                .flatMap(restaurant -> restaurant.getCuisines().stream())
                .collect(Collectors.toSet());
    }
}
