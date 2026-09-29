package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.promotion.PromotionContext;

public interface PromotionCondition {

    boolean isSatisfiedBy(PromotionContext context);
}
