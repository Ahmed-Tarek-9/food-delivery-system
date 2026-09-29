package com.masrdelivery.domain.promotion.strategy;

import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.InvalidPromotionException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class FixedAmountPromotion implements PromotionStrategy {
    private final BigDecimal fixedAmount;

    public FixedAmountPromotion(BigDecimal fixedAmount) {
        if (fixedAmount == null)
            throw new NullEntityException("FixedAmountPromotion fixedAmountBigDecimal");
        if (fixedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPromotionException("Fixed promotion amount must be positive.");
        }
        this.fixedAmount = fixedAmount;
    }

    @Override
    public BigDecimal calculateDiscount(PromotionContext context) {
        if (context == null)
            throw new NullEntityException("FixedAmountPromotion context");
        return fixedAmount.min(context.getOrder().getSubtotal());
    }
}
