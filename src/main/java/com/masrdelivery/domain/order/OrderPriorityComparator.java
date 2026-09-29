package com.masrdelivery.domain.order;

import com.masrdelivery.domain.customer.LoyaltyTier;

import java.util.Comparator;

public final class OrderPriorityComparator implements Comparator<Order> {

    @Override
    public int compare(Order o1, Order o2) {
        boolean isO1Gold = o1.getCustomer().getLoyaltyTier() == LoyaltyTier.GOLD;
        boolean isO2Gold = o2.getCustomer().getLoyaltyTier() == LoyaltyTier.GOLD;

        if (isO1Gold && !isO2Gold) return -1;
        if (!isO1Gold && isO2Gold) return 1;

        return o1.getPlacedAt().compareTo(o2.getPlacedAt());
    }
}
