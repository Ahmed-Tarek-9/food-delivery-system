package com.masrdelivery.repository;

import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.exception.DuplicateEntityException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.RiderNotFoundException;

import java.util.*;

public class RiderRepository implements Repository<String, Rider> {

    private final Map<String, Rider> riders;

    public RiderRepository() {
        this.riders = new HashMap<>();
    }

    @Override
    public void save(Rider rider) {
        if (rider == null) {
            throw new NullEntityException("Rider");
        }

        if (riders.containsKey(rider.getId())) {
            throw new DuplicateEntityException("Rider");
        }

        riders.put(rider.getId(), rider);
    }

    @Override
    public void update(Rider rider) {
        if (rider == null) {
            throw new NullEntityException("Rider");
        }

        if (!riders.containsKey(rider.getId())) {
            throw new RiderNotFoundException(rider.getId());
        }
        riders.put(rider.getId(), rider);
    }

    @Override
    public Optional<Rider> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Rider ID cannot be null or empty");
        }
        return Optional.ofNullable(riders.get(id));
    }

    @Override
    public Collection<Rider> findAll() {
        return List.copyOf(riders.values());
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Rider ID cannot be null or empty");
        }
        riders.remove(id);
    }

    public boolean existsByMobileNumber(MobileNumber mobileNumber) {
        return riders.values()
                .stream()
                .anyMatch(rider -> rider.getMobileNumber().equals(mobileNumber));
    }
}
