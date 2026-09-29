package com.masrdelivery.domain.promotion;

import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.promotion.condition.PromotionCondition;
import com.masrdelivery.domain.promotion.strategy.PromotionStrategy;
import com.masrdelivery.exception.DuplicateConditionException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;

import javax.naming.InvalidNameException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public  class Promotion {
    private final String code;
    private final String description;

    private final List<PromotionCondition> conditions;

    private final PromotionStrategy strategy;

    public Promotion(String code, String description, List<PromotionCondition> conditions, PromotionStrategy promotionStrategy) {
        if (code == null || code.isBlank()) {
            throw new InvalidStringException("Promotion code cannot be empty.");
        }
        if (promotionStrategy == null) {
            throw new NullEntityException("Promotion Strategy");
        }
        if (conditions != null) {
            Set<Class<? extends PromotionCondition>> conditionTypes = new HashSet<>();
            for (PromotionCondition condition : conditions) {
                if (condition == null) {
                    throw new NullEntityException("Promotion condition");
                }
                if (!conditionTypes.add(condition.getClass())) {
                    throw new DuplicateConditionException(condition.getClass().getSimpleName());
                }
            }
        }
        this.code = code;
        this.description = description;
        this.conditions = (conditions == null) ? List.of() :List.copyOf(conditions);
        if (conditions != null && conditions.stream().anyMatch(Objects::isNull)) {
            throw new NullEntityException("Promotion conditions");
        }
        this.strategy = promotionStrategy;
    }

    public String getCode() {
        return code;
    }

    public PromotionStrategy getStrategy() {
        return strategy;
    }

    public List<PromotionCondition> getConditions() {
        return conditions;
    }

    public String getDescription() {
        return description;
    }

    public boolean isApplicable(PromotionContext context) {
        if (context == null)
            throw new NullEntityException("PromotionContext context");
        return conditions.stream().allMatch(condition -> condition.isSatisfiedBy(context));
    }

    public BigDecimal calculateDiscount(PromotionContext context) {
        return strategy.calculateDiscount(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion promotion)) return false;
        return Objects.equals(code, promotion.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
