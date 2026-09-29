package com.masrdelivery.domain.promotion.strategy;

import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public class FreeDeliveryPromotion implements PromotionStrategy {

    @Override
    public BigDecimal calculateDiscount(PromotionContext context) {
        if (context == null)
            throw new NullEntityException("FreeDeliveryPromotion context");
        return BigDecimal.ZERO;
    }

    public boolean waivesDeliverFee() {
        return true;
    }
}
