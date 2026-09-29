package com.masrdelivery.domain.rider;

public enum RiderStatus {
    AVAILABLE {
        @Override
        public boolean canTransitionTo(RiderStatus nextStatus) {
            return nextStatus == RiderStatus.DELIVERING || nextStatus == RiderStatus.OFFLINE;
        }
    },
    DELIVERING {
        @Override
        public boolean canTransitionTo(RiderStatus nextStatus) {
            return nextStatus == RiderStatus.AVAILABLE;
        }
    },
    OFFLINE {
        @Override
        public boolean canTransitionTo(RiderStatus nextStatus) {
            return nextStatus == RiderStatus.AVAILABLE;
        }
    };

    public abstract boolean canTransitionTo(RiderStatus nextStatus);
}
