package com.masrdelivery.repository;

import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.exception.*;

import java.util.*;

public class PromotionRepository implements Repository <String, Promotion> {

    private final Map<String, Promotion> promotions;

    public PromotionRepository() {
        this.promotions = new HashMap<>();
    }

    @Override
    public void save(Promotion promotion) {
        if (promotion == null) {
            throw new NullEntityException("Promotion");
        }

        if (promotions.containsKey(promotion.getCode())) {
            throw new DuplicateEntityException("Promotion");
        }

        promotions.put(promotion.getCode(), promotion);
    }

    @Override
    public void update(Promotion promotion) {
        if (promotion == null) {
            throw new NullEntityException("Promotion");
        }

        if (!promotions.containsKey(promotion.getCode())) {
            throw new PromotionNotFoundException(promotion.getCode());
        }

        promotions.put(promotion.getCode(), promotion);
    }

    @Override
    public Optional<Promotion> findById(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidStringException("Promotion code cannot be null");
        }
        return Optional.ofNullable(promotions.get(code));
    }

    @Override
    public Collection<Promotion> findAll() {
        return List.copyOf(promotions.values());
    }

    @Override
    public void deleteById(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidStringException("Promotion code cannot be null");
        }
        promotions.remove(code);
    }
}
