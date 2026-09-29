package com.masrdelivery.service;

import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.domain.promotion.condition.PromotionCondition;
import com.masrdelivery.domain.promotion.factory.PromotionConfig;
import com.masrdelivery.domain.promotion.factory.PromotionStrategyFactory;
import com.masrdelivery.domain.promotion.strategy.PromotionStrategy;
import com.masrdelivery.exception.*;
import com.masrdelivery.repository.PromotionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        if (promotionRepository == null)
            throw new NullEntityException("PromotionRepository");
        this.promotionRepository = promotionRepository;
    }

    public Promotion createPromotion(
            String code,
            String description,
            PromotionConfig config,
            List<PromotionCondition> conditions
    ) {
        if (code == null || code.isBlank()) {
            throw new InvalidStringException("Promotion code cannot be null or blank");
        }
        if (config == null) {
            throw new NullEntityException("PromotionConfig");
        }
        if (promotionRepository.findById(code).isPresent()) {
            throw new DuplicatePromotionCodeException(code);
        }

        PromotionStrategy strategy = PromotionStrategyFactory.create(config);

        Promotion promotion = new Promotion(
                code,
                description,
                conditions,
                strategy
        );

        promotionRepository.save(promotion);

        return promotion;
    }

    public Promotion findPromotion(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidStringException("Promotion code cannot be null or blank");
        }

        return promotionRepository.findById(code).orElseThrow(
                () -> new PromotionNotFoundException(code)
        );
    }

    public List<Promotion> listPromotions() {
        return new ArrayList<>(promotionRepository.findAll());
    }

    public void deletePromotion(String code) {
        if(promotionRepository.findById(code).isPresent()) {
            promotionRepository.deleteById(code);
        } else {
            throw new PromotionNotFoundException(code);
        }
    }

    public boolean isPromotionApplicable(String promotionCode, Order order) {
        Promotion promotion = findPromotion(promotionCode);
        return promotion.isApplicable(new PromotionContext(order));
    }

    public BigDecimal applyPromotion(Promotion promotion, Order order) {
        PromotionContext promotionContext = new PromotionContext(order);

        if(!promotion.isApplicable(promotionContext)) {
            throw new PromotionNotApplicableException("Promotion code: " + promotion.getCode() + " is not applicable on order #" + order.getId());
        }

        return promotion.calculateDiscount(promotionContext);
    }
}
