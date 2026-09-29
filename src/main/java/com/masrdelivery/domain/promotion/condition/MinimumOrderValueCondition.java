package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class MinimumOrderValueCondition implements PromotionCondition {

    private final BigDecimal minimumOrderValue;

    public MinimumOrderValueCondition(BigDecimal minimumOrderValue) {
        if (minimumOrderValue == null) {
            throw new NullEntityException("MinimumOrderValue");
        }
        if (minimumOrderValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum order value must be greater than 0");
        }
        this.minimumOrderValue = minimumOrderValue;
    }

    @Override
    public boolean isSatisfiedBy(PromotionContext context) {
        if (context == null) {
            throw new NullEntityException("PromotionContext");
        }
        return context.getOrder().getSubtotal().compareTo(minimumOrderValue) >= 0;
    }
}
