package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.domain.rider.RiderStatus;
import com.masrdelivery.domain.rider.VehicleType;
import com.masrdelivery.exception.*;
import com.masrdelivery.repository.RiderRepository;
import com.masrdelivery.domain.common.id.RiderIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RiderService {
    private final RiderRepository riderRepository;
    private final RiderIdGenerator riderIdGenerator;

    public RiderService(RiderRepository riderRepository, RiderIdGenerator riderIdGenerator) {
        if (riderIdGenerator == null)
            throw new NullEntityException("RiderIdGenerator");
        if (riderRepository == null)
            throw new NullEntityException("RiderRepository");
        this.riderRepository = riderRepository;
        this.riderIdGenerator = riderIdGenerator;
    }

    public Rider createRider(String name, String mobileNumber, VehicleType vehicleType, District district) {
        if (riderRepository.existsByMobileNumber(new MobileNumber(mobileNumber))) {
            throw new DuplicateMobileNumberException("This mobile number was already used for another rider.");
        }

        Rider rider = new Rider.Builder()
                .id(riderIdGenerator.generate())
                .name(name)
                .mobileNumber(new MobileNumber(mobileNumber))
                .vehicleType(vehicleType)
                .currentDistrict(district)
                .build();

        riderRepository.save(rider);

        return rider;
    }

    public Rider findRider(String riderId) {
        if (riderId == null || riderId.isBlank()) {
            throw new InvalidStringException("Rider ID cannot be null or blank");
        }

        return riderRepository.findById(riderId).orElseThrow(
                () -> new RiderNotFoundException(riderId)
        );
    }

    public Rider findAvailableRiderByDistrict(District district) {
        if (district == null)
            throw new NullEntityException("District");

        return riderRepository
                .findAll()
                .stream()
                .filter(
                        rider -> rider.getRiderStatus() == RiderStatus.AVAILABLE
                )
                .filter(
                        rider -> rider.getCurrentDistrict() == district
                )
                .findFirst()
                .orElseThrow(
                        () -> new NoRiderAvailableException("No rider currently available in District: " + district)
                );
    }

    public List<Rider> listRiders() {
        return new ArrayList<>(riderRepository.findAll());
    }

    public List<Rider> listAvailableRiders() {
        return riderRepository
                .findAll()
                .stream()
                .filter(
                        rider -> rider.getRiderStatus() == RiderStatus.AVAILABLE
                ).collect(Collectors.toList());
    }

    public void updateStatus(String riderId, RiderStatus riderStatus) {
        if (riderStatus == null)
            throw new NullEntityException("RiderStatus");
        Rider rider = findRider(riderId);
        if (!rider.getRiderStatus().canTransitionTo(riderStatus)) {
            throw new IllegalTransitionException("Cannot change rider status from " + rider.getRiderStatus() + " to " + riderStatus);
        }
        rider.updateRiderStatus(riderStatus);
    }

    public void updateDistrict(String riderId, District district) {
        if (district == null)
            throw new NullEntityException("District");
        findRider(riderId).updateCurrentDistrict(district);
    }
}
