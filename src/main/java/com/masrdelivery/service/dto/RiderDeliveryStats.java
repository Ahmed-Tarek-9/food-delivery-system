package com.masrdelivery.service.dto;

import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.exception.NullEntityException;

public record RiderDeliveryStats(
        Rider rider,
        long completedDeliveries,
        double averageDeliveryMinutes
) {
    public RiderDeliveryStats{
        if (rider == null) {
            throw new NullEntityException("Rider");
        }
    }
}
