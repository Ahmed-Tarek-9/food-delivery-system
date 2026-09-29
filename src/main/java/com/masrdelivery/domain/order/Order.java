package com.masrdelivery.domain.order;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.exception.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Order {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address deliveryAddress;
    private final LocalDateTime placedAt;
    private LocalDateTime outForDeliveryAt;
    private  LocalDateTime deliveredAt;
    private final List<OrderItem> orderItems;
    private boolean isPaid;

    private Promotion promotion;
    private String deliveryNotes;
    private OrderStatus orderStatus;
    private Rider assignedRider;

    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal discount;
    private BigDecimal total;

    private Order(Builder builder) {
        if (builder.id == null || builder.id.isBlank()) {
            throw new InvalidStringException("Order ID cannot be empty.");
        }
        if (builder.customer == null) {
            throw new NullEntityException("Customer");
        }
        if (builder.restaurant == null) {
            throw new NullEntityException("Restaurant");
        }
        if (builder.deliveryAddress == null) {
            throw new NullEntityException("Address deliveryAddress");
        }
        this.id = builder.id;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.orderItems = new ArrayList<>(builder.orderItems);
        this.promotion = builder.promotion;
        this.deliveryNotes = builder.deliveryNotes;
        this.placedAt = LocalDateTime.now();
        this.orderStatus = OrderStatus.PLACED;
        this.isPaid = false;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public String getId() {
        return id;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public List<OrderItem> getOrderItems() {
        return List.copyOf(orderItems);
    }

    public OrderItem findOrderItem(String orderItemId) {

        if (orderItemId == null || orderItemId.isBlank())
            throw new InvalidStringException("OrderItem ID cannot be null or empty");

        if (orderItems.isEmpty())
            throw new EmptyCollectionException("The OrderItems list is empty");

        return orderItems.stream()
                .filter(
                        item -> item.getMenuItem().getId().equals(orderItemId)
                )
                .findAny()
                .orElse(null);
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public LocalDateTime getOutForDeliveryAt() {
        return outForDeliveryAt;
    }

    public void setOutForDeliveryAt(LocalDateTime outForDeliveryAt) {
        if (outForDeliveryAt == null) {
            throw new NullEntityException("Date");
        }
        this.outForDeliveryAt = outForDeliveryAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        if (deliveredAt == null) {
            throw new NullEntityException("Date");
        }

        this.deliveredAt = deliveredAt;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public void addOrderItem(OrderItem orderItem) {

        if (orderItem == null)
            throw new NullEntityException("OrderItem");

        if (orderItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidQuantityException(orderItem.getQuantity());

        this.orderItems.add(orderItem);
    }

    public void removeOrderItem(OrderItem orderItem) {

        if (orderItem == null)
            throw new NullEntityException("OrderItem");

        this.orderItems.remove(orderItem);
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Rider getAssignedRider() {
        return assignedRider;
    }

    public void assignRider(Rider rider) {
        if (rider == null) {
            throw new NullEntityException("Rider");
        }
        this.assignedRider = rider;
    }

    public BigDecimal getSubtotal() {
        return orderItems.stream().map(OrderItem::calculateLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private String id;
        private Customer customer;
        private Restaurant restaurant;
        private Address deliveryAddress;
        private List<OrderItem> orderItems = new ArrayList<>();
        private Promotion promotion;
        private String deliveryNotes;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder restaurant(Restaurant restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public Builder deliveryAddress(Address deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder items(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public Builder item(MenuItem menuItem, BigDecimal quantity) {
            if (quantity.compareTo(new BigDecimal(0)) <= 0) {
                throw new InvalidQuantityException(quantity);
            } else {
                this.orderItems.add(new OrderItem(menuItem, quantity));
            }
            return this;
        }

        public Builder deliveryNotes(String deliveryNotes) {
            this.deliveryNotes = deliveryNotes;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
