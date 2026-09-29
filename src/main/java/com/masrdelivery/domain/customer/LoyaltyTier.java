package com.masrdelivery.domain.customer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum LoyaltyTier {
    BRONZE {
        @Override
        public BigDecimal applyDeliveryBenefit(BigDecimal deliveryFee) {
            return deliveryFee;
        }
    },
    SILVER {
        @Override
        public BigDecimal applyDeliveryBenefit(BigDecimal deliveryFee) {
            return deliveryFee.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP);
        }
    },
    GOLD {
        @Override
        public BigDecimal applyDeliveryBenefit(BigDecimal deliveryFee) {
            return BigDecimal.ZERO;
        }
    };

    public abstract BigDecimal applyDeliveryBenefit(BigDecimal deliveryFee);

    public static LoyaltyTier fromOrderCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Order count cannot be negative");
        }
        if (count >= 30) {
            return GOLD;
        } else if (count >= 10) {
            return SILVER;
        }
        return BRONZE;
    }
}
