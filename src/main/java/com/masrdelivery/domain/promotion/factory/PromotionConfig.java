package com.masrdelivery.domain.promotion.factory;

import com.masrdelivery.domain.promotion.common.PromotionType;
import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;

public record PromotionConfig(
        PromotionType promotionType,
        BigDecimal value,
        BigDecimal maxDiscount
) {
    public PromotionConfig {
        if (promotionType == null)
            throw new NullEntityException("PromotionType");
    }

    public static PromotionConfig percentage(BigDecimal percentage, BigDecimal maxDiscount) {

        return new PromotionConfig(
                PromotionType.PERCENTAGE,
                percentage,
                maxDiscount
        );
    }

    public static PromotionConfig fixedAmount(BigDecimal amount) {

        return new PromotionConfig(
                PromotionType.FIXED_AMOUNT,
                amount,
                null
        );
    }

    public static PromotionConfig freeDelivery() {

        return new PromotionConfig(
                PromotionType.FREE_DELIVERY,
                null,
                null
        );
    }
}
