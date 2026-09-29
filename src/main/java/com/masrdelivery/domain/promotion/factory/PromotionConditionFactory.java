package com.masrdelivery.domain.promotion.factory;

import com.masrdelivery.domain.promotion.condition.*;
import com.masrdelivery.exception.NullEntityException;

public final class PromotionConditionFactory {

    private PromotionConditionFactory() {
    }

    public static PromotionCondition create(PromotionConditionConfig config) {
        if (config == null) {
            throw new NullEntityException("PromotionConditionConfig");
        }
        return switch (config.type()) {
            case DISTRICT -> new DistrictCondition(config.district());
            case EXPIRY_DATE -> new ExpiryDateCondition(config.expiryDate());
            case FIRST_ORDER -> new FirstOrderCondition();
            case LOYALTY_TIER -> new LoyaltyTierCondition(config.loyaltyTier());
            case MINIMUM_ORDER_VALUE -> new MinimumOrderValueCondition(config.minimumOrderValue());
        };
    }
}
