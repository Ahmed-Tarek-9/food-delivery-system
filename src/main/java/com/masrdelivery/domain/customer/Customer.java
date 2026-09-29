package com.masrdelivery.domain.customer;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.exception.*;

import java.math.BigDecimal;
import java.util.*;

public class Customer {
    private final String id;
    private String name;
    private MobileNumber mobileNumber;
    private BigDecimal walletBalance;
    private final Set<Address> addresses;
    private final Deque<String> recentSearches;
    private int totalPlacedOrders;
    private int completedOrders;

    private Customer(Builder builder) {
        if (builder.id == null || builder.id.isBlank()) {
            throw new InvalidStringException("Customer ID cannot be empty.");
        }
        if (builder.name == null || builder.name.isBlank()) {
            throw new InvalidStringException("Customer name cannot be empty.");
        }
        if (builder.mobileNumber == null) {
            throw new NullEntityException("Mobile Number");
        }
        this.id = builder.id;
        this.name = builder.name;
        this.mobileNumber = builder.mobileNumber;
        this.walletBalance = builder.walletBalance;
        this.addresses = new HashSet<>(builder.addresses);
        this.recentSearches = new ArrayDeque<>(5);
        this.totalPlacedOrders = 0;
        this.completedOrders = 0;
    }

    public String getId() {
        return id;
    }

    public Set<Address> getAddresses() {
        return Set.copyOf(addresses);
    }

    public void addAddress(Address address) {
        if (address == null) {
            throw new NullEntityException("Address");
        }
        this.addresses.add(address);
    }

    public void removeAddress(Address address) {
        if (address == null) {
            throw new NullEntityException("Address");
        }
        if(!addresses.remove(address)) {
            throw new AddressNotFoundException("Cannot remove an address that doesn't exist.");
        }
    }

    public String getName() {
        return name;
    }

    public MobileNumber getMobileNumber() {
        return mobileNumber;
    }

    public void updateMobileNumber(MobileNumber mobileNumber) {
        if (mobileNumber == null) {
            throw new NullEntityException("MobileNumber");
        }
        this.mobileNumber = mobileNumber;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void rechargeWallet(BigDecimal amount) {
        if (amount == null) {
            throw new NullEntityException("BigDecimal amount");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRechargeAmountException("Recharge amount must be positive.");
        }
        this.walletBalance = this.walletBalance.add(amount);
    }

    public void pay(BigDecimal amount) {
        if (amount == null) {
            throw new NullEntityException("BigDecimal amount");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidWithdrawalAmountException("Can only withdraw a positive amount");

        if (amount.compareTo(this.walletBalance) > 0)
            throw new InsufficientBalanceException("Customer with ID: " + this.id + " has insufficient balance");

        this.walletBalance = this.walletBalance.subtract(amount);
    }

    public List<String> getRecentSearches() {
        return List.copyOf(recentSearches);
    }

    public void addSearch(String search) {
        if (search == null || search.isBlank()) {
            throw new InvalidStringException("Search query cannot be empty.");
        }
        if (recentSearches.contains(search)) {
            recentSearches.remove(search);
            recentSearches.addFirst(search.trim());
            return;
        }
        if (recentSearches.size() == 5) {
            recentSearches.removeLast();
        }
        recentSearches.addFirst(search.trim());
    }

    public void incrementTotalPlacedOrders() {
        this.totalPlacedOrders++;
    }

    public void decrementTotalPlacedOrders() {
        this.totalPlacedOrders--;
    }

    public int getTotalPlacedOrders() {
        return totalPlacedOrders;
    }

    public void incrementCompletedOrders() {
        this.completedOrders++;
    }

    public void decrementCompletedOrders() {
        this.completedOrders--;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public LoyaltyTier getLoyaltyTier() {
        return LoyaltyTier.fromOrderCount(completedOrders);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private String id;
        private String name;
        private MobileNumber mobileNumber;
        private BigDecimal walletBalance = BigDecimal.ZERO;
        private final Set<Address> addresses = new HashSet<>();

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

        public Builder walletBalance(BigDecimal walletBalance) {
            this.walletBalance = walletBalance;
            return this;
        }

        public Builder address(Address address) {
            if (address != null) {
                addresses.add(address);
            }
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }

}
