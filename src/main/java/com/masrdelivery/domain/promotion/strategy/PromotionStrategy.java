package com.masrdelivery.domain.promotion.strategy;

import com.masrdelivery.domain.promotion.PromotionContext;

import java.math.BigDecimal;

public interface PromotionStrategy {

    public BigDecimal calculateDiscount(PromotionContext context);
}
