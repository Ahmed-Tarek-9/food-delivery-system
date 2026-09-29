package com.masrdelivery.domain.rider;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.exception.IllegalTransitionException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;

import java.util.Objects;

public class Rider {
    private final String id;
    private final String name;
    private MobileNumber mobileNumber;
    private VehicleType vehicleType;
    private District currentDistrict;
    private RiderStatus riderStatus;
    private int completedOrders;
    private Order activeOrder;

    private Rider(Builder builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new InvalidStringException("Rider name cannot be empty.");
        }
        if (builder.mobileNumber == null) {
            throw new NullEntityException("Mobile Number");
        }
        if (builder.id == null || builder.id.isBlank()) {
            throw new InvalidStringException("Rider id cannot be empty.");
        }
        if (builder.vehicleType == null) {
            throw new NullEntityException("Vehicle Type");
        }
        if (builder.currentDistrict == null) {
            throw new NullEntityException("District");
        }
        this.id = builder.id;
        this.name = builder.name;
        this.mobileNumber = builder.mobileNumber;
        this.vehicleType = builder.vehicleType;
        this.currentDistrict = builder.currentDistrict;
        this.riderStatus = RiderStatus.AVAILABLE;
        this.completedOrders = 0;
        this.activeOrder = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MobileNumber getMobileNumber() {
        return mobileNumber;
    }

    public void updateMobileNumber(MobileNumber mobileNumber) {
        if (mobileNumber == null)
            throw new NullEntityException("Mobile Number");
        this.mobileNumber = mobileNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void updateVehicleType(VehicleType vehicleType) {
        if (vehicleType == null)
            throw new NullEntityException("Vehicle Type");
        this.vehicleType = vehicleType;
    }

    public void completeOrder() {
        this.completedOrders++;
    }

    public void setActiveOrder(Order activeOrder) {
        if (activeOrder == null)
            throw new NullEntityException("Order");
        this.activeOrder = activeOrder;
        this.riderStatus = RiderStatus.DELIVERING;
    }

    public void clearActiveOrder() {
        this.activeOrder = null;
        this.riderStatus = RiderStatus.AVAILABLE;
    }

    public District getCurrentDistrict() {
        return currentDistrict;
    }

    public void updateCurrentDistrict(District district) {
        if (district == null)
            throw new NullEntityException("District");
        this.currentDistrict = district;
    }

    public RiderStatus getRiderStatus() {
        return riderStatus;
    }

    public void updateRiderStatus(RiderStatus riderStatus) {
        if(riderStatus == null)
            throw new NullEntityException("Rider Status");

        if (!this.riderStatus.canTransitionTo(riderStatus)) {
            throw new IllegalTransitionException("Invalid transition from " + this.riderStatus + " to " + riderStatus);
        }
        this.riderStatus = riderStatus;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public Order getActiveOrder() {
        return activeOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rider rider)) return false;
        return Objects.equals(id, rider.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private String id;
        private String name;
        private MobileNumber mobileNumber;
        private VehicleType vehicleType;
        private District currentDistrict;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mobileNumber(MobileNumber mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder vehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Builder currentDistrict(District currentDistrict) {
            this.currentDistrict = currentDistrict;
            return this;
        }

        public Rider build() {
            return new Rider(this);
        }
    }
}
