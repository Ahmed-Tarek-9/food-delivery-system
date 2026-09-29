package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.customer.LoyaltyTier;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

public class LoyaltyTierCondition implements PromotionCondition {

    private final LoyaltyTier requiredLoyaltyTier;

    public LoyaltyTierCondition(LoyaltyTier requiredLoyaltyTier) {
        if (requiredLoyaltyTier == null) {
            throw new NullEntityException("LoyaltyTier");
        }
        this.requiredLoyaltyTier = requiredLoyaltyTier;
    }

    @Override
    public boolean isSatisfiedBy(PromotionContext context) {
        if (context == null) {
            throw new NullEntityException("PromotionContext");
        }
        return context.getCustomer().getLoyaltyTier() == requiredLoyaltyTier;
    }
}
