package com.masrdelivery.domain.promotion.factory;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.LoyaltyTier;
import com.masrdelivery.domain.promotion.common.PromotionConditionType;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionConditionConfig(
        PromotionConditionType type,
        District district,
        LocalDate expiryDate,
        LoyaltyTier loyaltyTier,
        BigDecimal minimumOrderValue
) {
    public PromotionConditionConfig {
        if (type == null) {
            throw new NullEntityException("PromotionConditionType");
        }
    }

    public static PromotionConditionConfig district(District district) {

        return new PromotionConditionConfig(PromotionConditionType.DISTRICT, district, null, null, null);
    }

    public static PromotionConditionConfig expiryDate(LocalDate expiryDate) {

        return new PromotionConditionConfig(PromotionConditionType.EXPIRY_DATE, null, expiryDate, null, null);
    }

    public static PromotionConditionConfig firstOrder() {

        return new PromotionConditionConfig(PromotionConditionType.FIRST_ORDER, null, null, null, null);
    }

    public static PromotionConditionConfig loyaltyTier(LoyaltyTier loyaltyTier) {

        return new PromotionConditionConfig(PromotionConditionType.LOYALTY_TIER, null, null, loyaltyTier, null);
    }

    public static PromotionConditionConfig minimumOrderValue(BigDecimal minimumOrderValue) {

        return new PromotionConditionConfig(PromotionConditionType.MINIMUM_ORDER_VALUE, null, null, null, minimumOrderValue);
    }
}
