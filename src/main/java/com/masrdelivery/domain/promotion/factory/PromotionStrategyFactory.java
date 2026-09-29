package com.masrdelivery.domain.promotion.factory;

import com.masrdelivery.domain.promotion.strategy.FixedAmountPromotion;
import com.masrdelivery.domain.promotion.strategy.FreeDeliveryPromotion;
import com.masrdelivery.domain.promotion.strategy.PercentagePromotion;
import com.masrdelivery.domain.promotion.strategy.PromotionStrategy;
import com.masrdelivery.exception.NullEntityException;

public final class PromotionStrategyFactory {

    private PromotionStrategyFactory() {

    }

    public static PromotionStrategy create(PromotionConfig config) {
        if (config == null)
            throw new NullEntityException("PromotionConfig");

        return switch (config.promotionType()) {
            case PERCENTAGE -> new PercentagePromotion(config.value(), config.maxDiscount());
            case FIXED_AMOUNT -> new FixedAmountPromotion(config.value());
            case FREE_DELIVERY -> new FreeDeliveryPromotion();
        };
    }
}
