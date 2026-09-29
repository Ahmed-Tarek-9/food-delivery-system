package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

public class FirstOrderCondition implements PromotionCondition {

    @Override
    public boolean isSatisfiedBy(PromotionContext context) {
        if (context == null) {
            throw new NullEntityException("PromotionContext");
        }
        return context.getOrder().getCustomer().getTotalPlacedOrders() == 0;
    }
}
