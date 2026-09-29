package com.masrdelivery.domain.promotion.condition;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.exception.NullEntityException;

public class DistrictCondition implements PromotionCondition {

    private final District district;

    public DistrictCondition(District district) {
        if (district == null) {
            throw new NullEntityException("District");
        }
        this.district = district;
    }

    @Override
    public boolean isSatisfiedBy(PromotionContext context) {
        if (context == null) {
            throw new NullEntityException("PromotionContext");
        }
        return context.getOrder().getDeliveryAddress().district().equals(district);
    }
}
