package com.masrdelivery.domain.promotion.strategy;

import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.InvalidPromotionException;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentagePromotion implements PromotionStrategy {
    private final BigDecimal percentage;
    private final BigDecimal maximumDiscount;

    public PercentagePromotion(BigDecimal percentage, BigDecimal maximumDiscount) {
        if (percentage == null || maximumDiscount == null)
            throw new NullEntityException("PercentagePromotion BigDecimal");

        BigDecimal rate = percentage.compareTo(BigDecimal.ONE) > 0 ?
                percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP) : percentage;

        if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.valueOf(1)) > 0) {
            throw new InvalidPromotionException("Percentage discount must be between 1 and 100");
        }
        if (maximumDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPromotionException("Maximum discount amount must be positive.");
        }
        this.percentage = rate;
        this.maximumDiscount = maximumDiscount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDiscount(PromotionContext context) {
        if (context == null)
            throw new NullEntityException("PercentagePromotion context");
        BigDecimal discount = context.getOrder().getSubtotal().multiply(percentage).setScale(2, RoundingMode.HALF_UP);
        return discount.compareTo(maximumDiscount) > 0 ? maximumDiscount : discount;
    }
}
