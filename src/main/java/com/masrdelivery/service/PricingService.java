package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.LoyaltyTier;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.domain.promotion.strategy.FreeDeliveryPromotion;
import com.masrdelivery.exception.IllegalPricingException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.repository.DistrictDistanceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricingService {

    private final DistrictDistanceRepository districtDistanceRepository;
    private static final BigDecimal BASE_DELIVERY_FEE = new BigDecimal("15.00");
    private static final BigDecimal EXTRA_KM_CHARGE = new BigDecimal("3.00");
    private static final int INCLUDED_DISTANCE_KM = 3;

    public PricingService(DistrictDistanceRepository districtDistanceRepository) {
        if (districtDistanceRepository == null)
            throw new NullEntityException("DistrictDistanceRepository");

        this.districtDistanceRepository = districtDistanceRepository;
    }

    public BigDecimal calculateDeliveryFee(Order order) {
        if (order == null)
            throw new NullEntityException("Order");

        Promotion promotion = order.getPromotion();

        if (promotion != null && promotion.isApplicable(new PromotionContext(order))) {
            if (promotion.getStrategy() instanceof FreeDeliveryPromotion) {
                return BigDecimal.ZERO;
            }
        }

        District restaurantDistrict = order.getRestaurant().getDistrict();
        District customerDistrict = order.getDeliveryAddress().district();

        LoyaltyTier loyaltyTier = order.getCustomer().getLoyaltyTier();

        int distance = districtDistanceRepository.getDistance(restaurantDistrict, customerDistrict);

        int extraKm = Math.max(0, distance - INCLUDED_DISTANCE_KM);
        BigDecimal fee = BASE_DELIVERY_FEE.add(BigDecimal.valueOf(extraKm)
                .multiply(EXTRA_KM_CHARGE));

        return loyaltyTier.applyDeliveryBenefit(fee);
    }

    public BigDecimal calculateServiceFee(Order order) {
        if (order == null)
            throw new NullEntityException("Order");

        BigDecimal subtotal = order.getSubtotal();

        return subtotal.multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscount(Order order) {
        if (order == null)
            throw new NullEntityException("Order");

        Promotion promotion = order.getPromotion();

        if (promotion == null)
            return BigDecimal.ZERO;

        PromotionContext promotionContext = new PromotionContext(order);

        if (!promotion.isApplicable(promotionContext))
            return BigDecimal.ZERO;

        return promotion.calculateDiscount(promotionContext);
    }

    public void applyPricing(Order order) {
        if (order == null)
            throw new NullEntityException("Order");
        if (!order.getOrderStatus().isModifiable())
            throw new IllegalPricingException("Cannot apply new pricing to an order of status: " + order.getOrderStatus());

        BigDecimal subtotal = order.getSubtotal();
        BigDecimal deliveryFee = calculateDeliveryFee(order);
        BigDecimal serviceFee = calculateServiceFee(order);
        BigDecimal discount = calculateDiscount(order);
        BigDecimal total = subtotal
                .add(deliveryFee)
                .add(serviceFee)
                .subtract(discount)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        order.setDeliveryFee(deliveryFee);
        order.setServiceFee(serviceFee);
        order.setDiscount(discount);
        order.setTotal(total);
    }
}
