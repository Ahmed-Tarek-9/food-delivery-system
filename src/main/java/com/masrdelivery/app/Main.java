package com.masrdelivery.app;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.domain.promotion.condition.DistrictCondition;
import com.masrdelivery.domain.promotion.condition.MinimumOrderValueCondition;
import com.masrdelivery.domain.promotion.condition.PromotionCondition;
import com.masrdelivery.domain.promotion.strategy.PercentagePromotion;
import com.masrdelivery.domain.promotion.strategy.PromotionStrategy;
import com.masrdelivery.domain.restaurant.*;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.domain.rider.VehicleType;
import com.masrdelivery.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}