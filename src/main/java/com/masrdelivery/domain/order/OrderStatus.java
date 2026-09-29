package com.masrdelivery.domain.order;

public enum OrderStatus {
    PLACED {
        @Override
        public boolean isModifiable() {
            return true;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == ACCEPTED || newStatus == CANCELLED;
        }
    },
    ACCEPTED {
        @Override
        public boolean isModifiable() {
            return true;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == PREPARING || newStatus == CANCELLED;
        }
    },
    PREPARING {
        @Override
        public boolean isModifiable() {
            return true;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == READY || newStatus == CANCELLED;
        }
    },
    READY {
        @Override
        public boolean isModifiable() {
            return false;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == ASSIGNED;
        }
    },
    ASSIGNED {
        @Override
        public boolean isModifiable() {
            return false;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == OUT_FOR_DELIVERY;
        }
    },
    OUT_FOR_DELIVERY {
        @Override
        public boolean isModifiable() {
            return false;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DELIVERED;
        }
    },
    DELIVERED {
        @Override
        public boolean isModifiable() {
            return false;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean isModifiable() {
            return false;
        }
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    };

    public abstract boolean isModifiable();
    public abstract boolean canTransitionTo(OrderStatus newStatus);
}
