package com.masrdelivery.domain.common;

import com.masrdelivery.exception.InvalidAddressException;
import com.masrdelivery.exception.NullEntityException;

public record Address(District district, String details) {

    public Address {
        if (district == null)
            throw new NullEntityException("District");

        if (details == null || details.isBlank()) {
            throw new InvalidAddressException("The details of the address cannot be empty.");
        }

        details = details.trim();
    }
}
