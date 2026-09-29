package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

import java.time.LocalDate;

public class ExpiryDateCondition implements PromotionCondition {
    private final LocalDate expiryDate;

    public ExpiryDateCondition(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new NullEntityException("ExpiryDate");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date for the promotion cannot be before today.");
        }
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isSatisfiedBy(PromotionContext context) {
        if (context == null) {
            throw new NullEntityException("PromotionContext");
        }
        return !context.getOrder().getPlacedAt().toLocalDate().isAfter(expiryDate);
    }
}
