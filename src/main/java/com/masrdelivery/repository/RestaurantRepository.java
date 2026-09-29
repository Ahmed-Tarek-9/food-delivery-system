package com.masrdelivery.repository;

import com.masrdelivery.domain.restaurant.Cuisine;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.exception.DuplicateEntityException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.RestaurantNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class RestaurantRepository implements Repository<String, Restaurant> {

    private final Map<String, Restaurant> restaurants;

    public RestaurantRepository() {
        this.restaurants = new HashMap<>();
    }


    @Override
    public void save(Restaurant restaurant) {
        if (restaurant == null) {
            throw new NullEntityException("Restaurant");
        }

        if (restaurants.containsKey(restaurant.getId())) {
            throw new DuplicateEntityException("Restaurant");
        }

        restaurants.put(restaurant.getId(), restaurant);
    }

    @Override
    public void update(Restaurant restaurant) {
        if (restaurant == null) {
            throw new NullEntityException("Restaurant");
        }
        if (!restaurants.containsKey(restaurant.getId())) {
            throw new RestaurantNotFoundException(restaurant.getId());
        }
        restaurants.put(restaurant.getId(), restaurant);
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Restaurant ID cannot be null or empty");
        }
        return Optional.ofNullable(restaurants.get(id));
    }

    @Override
    public Collection<Restaurant> findAll() {
        return List.copyOf(restaurants.values());
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Restaurant ID cannot be null or empty");
        }
        restaurants.remove(id);
    }

    public Collection<Restaurant> findAllSortedByRating() {
        return restaurants.values()
                .stream()
                .sorted(
                        Comparator
                                .comparing(Restaurant::getRating)
                                .reversed()
                                .thenComparing(Restaurant::getName)
                                .thenComparing(Restaurant::getId)
                )
                .collect(Collectors.toList());
    }

    public Set<Cuisine> findCuisines() {
        return restaurants.values().stream().flatMap(r -> r.getCuisines().stream()).collect(Collectors.toSet());
    }
}
