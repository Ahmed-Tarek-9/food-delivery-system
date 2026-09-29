package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.restaurant.Cuisine;

import java.math.BigDecimal;

public record RestaurantFilterCriteria(
        District district,
        Cuisine cuisine,
        BigDecimal minRating,
        BigDecimal maxPriceCieling
) {
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private District district;
        private Cuisine cuisine;
        private BigDecimal minRating;
        private BigDecimal maxPriceCieling;

        public Builder district(District district) {
            this.district = district;
            return this;
        }

        public Builder cuisine(Cuisine cuisine) {
            this.cuisine = cuisine;
            return this;
        }

        public Builder minRating(BigDecimal minRating) {
            this.minRating = minRating;
            return this;
        }

        public Builder maxPriceCieling(BigDecimal maxPriceCieling) {
            this.maxPriceCieling = maxPriceCieling;
            return this;
        }

        public RestaurantFilterCriteria build() {
            return new RestaurantFilterCriteria(district, cuisine, minRating, maxPriceCieling);
        }
    }
}
