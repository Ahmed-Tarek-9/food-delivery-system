package com.masrdelivery.domain.common;

import com.masrdelivery.exception.NullEntityException;

public record DistrictPair(District source, District destination) {

    public DistrictPair {
        if (source == null || destination == null)
            throw new NullEntityException("District");

        if (source.ordinal() > destination.ordinal()) {
            District temp = source;
            source = destination;
            destination = temp;
        }
    }
}
