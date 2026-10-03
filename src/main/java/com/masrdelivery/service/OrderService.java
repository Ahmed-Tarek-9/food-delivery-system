package com.masrdelivery.service;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderItem;
import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.promotion.Promotion;
import com.masrdelivery.domain.promotion.PromotionContext;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.domain.rider.RiderStatus;
import com.masrdelivery.exception.*;
import com.masrdelivery.repository.OrderRepository;
import com.masrdelivery.domain.common.id.OrderIdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final PromotionService promotionService;
    private final PricingService pricingService;
    private final RiderService riderService;
    private final OrderIdGenerator orderIdGenerator;
    private final OrderDispatchQueue orderDispatchQueue;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            RestaurantService restaurantService,
            PromotionService promotionService,
            PricingService pricingService,
            RiderService riderService,
            OrderIdGenerator orderIdGenerator,
            OrderDispatchQueue orderDispatchQueue
    ) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.promotionService = promotionService;
        this.pricingService = pricingService;
        this.riderService = riderService;
        this.orderIdGenerator = orderIdGenerator;
        this.orderDispatchQueue = orderDispatchQueue;
    }

    public Order createOrder(
            String customerId,
            String restaurantId,
            Address deliveryAddress,
            List<OrderItem> items,
            String promotionCode,
            String deliveryNotes
    ) {
        Customer customer = customerService.findCustomer(customerId);
        Restaurant restaurant = restaurantService.findRestaurant(restaurantId);

        if (!customer.getAddresses().contains(deliveryAddress)) {
            throw new InvalidAddressException("Delivery address does not belong to customer: " + customerId);
        }

        if (!restaurant.isOpen())
            throw new RestaurantClosedException();

        for (OrderItem item : items) {
            if (!item.getMenuItem().isAvailable())
                throw new ItemUnavailableException(item.getMenuItem());

            if (item.getQuantity().compareTo(item.getMenuItem().getStock()) > 0)
                throw new InsufficientStockException(item.getMenuItem().getName());

            if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                throw new InvalidQuantityException(item.getQuantity());

            MenuItem menuItem = restaurant.findMenuItem(item.getMenuItem().getId());

            if (menuItem == null)
                throw new ItemNotInRestaurantException(
                        "Item with ID: " +
                                item.getMenuItem().getId() +
                                "doesn't exist within restaurant with ID: " +
                                restaurantId
                );
        }

        for (OrderItem item : items) {
            item.getMenuItem().deductStock(item.getQuantity());
        }

        Order order = new Order.Builder()
                .id(orderIdGenerator.generate())
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(deliveryAddress)
                .items(items)
                .deliveryNotes(deliveryNotes)
                .build();

        if (promotionCode != null) {
            Promotion promotion = promotionService.findPromotion(promotionCode);

            if (!promotion.isApplicable(new PromotionContext(order)))
                throw new PromotionNotApplicableException("Promo code: " +
                        promotion.getCode() +
                        " is not applicable on Order ID: " +
                        order.getId());

            order.setPromotion(promotion);
        }

        pricingService.applyPricing(order);

        order.getCustomer().incrementTotalPlacedOrders();

        orderRepository.save(order);

        return order;
    }

    public Order findOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidStringException("Order ID cannot be null or blank");
        }
        return orderRepository
                .findById(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );
    }

    public List<Order> listOrders() {
        return new ArrayList<>(orderRepository.findAll());
    }

    public void applyPromotion(String orderId, String promotionCode) {
        Order order = findOrder(orderId);
        Promotion promotion = promotionService.findPromotion(promotionCode);

        if (!promotion.isApplicable(new PromotionContext(order)))
            throw new PromotionNotApplicableException("Promo code: " + promotion.getCode() + " is not applicable on Order ID: " + orderId);

        if(!order.getOrderStatus().isModifiable())
            throw new PromotionNotApplicableException("A promo code cannot be applied on an order with status: " + order.getOrderStatus());

        order.setPromotion(promotion);

        pricingService.applyPricing(order);
    }

    public void cancelOrder(String orderId) {
        Order order = findOrder(orderId);

        if (!order.getOrderStatus().isModifiable())
            throw new IllegalOrderModificationException("Cannot cancel order of status: " + order.getOrderStatus());

        for (OrderItem item : order.getOrderItems()) {
            item.getMenuItem().restock(item.getQuantity());
        }

        if (order.isPaid()) {
            order.getCustomer().rechargeWallet(order.getTotal());
            order.setPaid(false);
        }


        order.getCustomer().decrementTotalPlacedOrders();
        order.setTotal(BigDecimal.ZERO);
        order.setOrderStatus(OrderStatus.CANCELLED);
    }

    public void addOrderItem(String orderId, OrderItem orderItem) {
        Order order = findOrder(orderId);

        if (order.isPaid()) {
            throw new IllegalOrderModificationException("Cannot add items to a paid order");
        }

        if (!order.getOrderStatus().isModifiable()) {
            throw new IllegalOrderModificationException("Cannot add items to an order with status: " + order.getOrderStatus());
        }

        Restaurant restaurant = order.getRestaurant();

        MenuItem menuItem = restaurant.findMenuItem(orderItem.getMenuItem().getId());

        if (menuItem == null)
            throw new ItemNotInRestaurantException(
                    "Item with ID: " +
                            orderItem.getMenuItem().getId() +
                            "doesn't exist within restaurant with ID: " +
                            restaurant.getId()
            );

        if (orderItem.getQuantity().compareTo(menuItem.getStock()) > 0)
            throw new InsufficientStockException(menuItem.getName());

        order.addOrderItem(orderItem);
        menuItem.deductStock(orderItem.getQuantity());

        pricingService.applyPricing(order);
    }

    public void removeOrderItem(String orderId, String menuItemId) {
        Order order = findOrder(orderId);

        if (order.isPaid())
            throw new OrderAlreadyPaidException("Cannot edit an order that has been already paid.");

        if (!order.getOrderStatus().isModifiable())
            throw new IllegalOrderModificationException("Cannot edit order of status: " + order.getOrderStatus());

        OrderItem orderItem = order.findOrderItem(menuItemId);
        order.removeOrderItem(orderItem);
        orderItem.getMenuItem().restock(orderItem.getQuantity());

        pricingService.applyPricing(order);
    }

    public void payOrder(String orderId) {
        Order order = findOrder(orderId);

        if (order.isPaid())
            throw new OrderAlreadyPaidException(orderId);

        order.getCustomer().pay(order.getTotal());

        order.setPaid(true);
    }

    public void assignRider(String orderId) {
        Order order = findOrder(orderId);

        if (order.getOrderStatus() != OrderStatus.READY)
            throw new IllegalRiderAssignmentException("Cannot assign rider to an order that is not READY");

        Rider rider = riderService.findAvailableRiderByDistrict(order.getRestaurant().getDistrict());

        if (rider == null)
            throw new NoRiderAvailableException("No riders available in District: "
                    + order.getRestaurant().getDistrict()
                    );

        order.assignRider(rider);
        rider.updateRiderStatus(RiderStatus.DELIVERING);
        rider.setActiveOrder(order);
        order.setOrderStatus(OrderStatus.ASSIGNED);
    }

    public void updateStatus(String orderId, OrderStatus newStatus) {
        Order order = findOrder(orderId);

        if (!order.getOrderStatus().canTransitionTo(newStatus))
            throw new IllegalOrderTransitionException(order.getOrderStatus(), newStatus);

        order.setOrderStatus(newStatus);
    }

    public void markOrderReady(String orderId) {
        Order order = findOrder(orderId);

        if (!order.getOrderStatus().canTransitionTo(OrderStatus.READY)) {
            throw new IllegalOrderTransitionException(order.getOrderStatus(), OrderStatus.READY);
        }

        order.setOrderStatus(OrderStatus.READY);
        orderDispatchQueue.enqueueReadyOrder(order);
    }

    public Optional<Order> dispatchNextReadyOrder() {
        Optional<Order> nextOrderOpt = orderDispatchQueue.pollNextOrder();

        if (nextOrderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = nextOrderOpt.get();

        try {
            Rider rider = riderService.findAvailableRiderByDistrict(order.getRestaurant().getDistrict());
            order.assignRider(rider);
            rider.updateRiderStatus(RiderStatus.DELIVERING);
            rider.setActiveOrder(order);
            order.setOrderStatus(OrderStatus.ASSIGNED);
            return Optional.of(order);
        } catch (NoRiderAvailableException e) {
            orderDispatchQueue.enqueueReadyOrder(order);
            throw e;
        }
    }

    public Optional<Order> claimNextOrderForRider(Rider rider) {
        if (rider == null) {
            throw new NullEntityException("Rider");
        }

        if (rider.getRiderStatus() != RiderStatus.AVAILABLE) {
            throw new IllegalRiderAssignmentException("Rider must be AVAILABLE to claim an order.");
        }

        Optional<Order> orderOpt = orderDispatchQueue.pollNextForDistrict(rider.getCurrentDistrict());

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        order.assignRider(rider);
        rider.updateRiderStatus(RiderStatus.DELIVERING);
        rider.setActiveOrder(order);
        order.setOrderStatus(OrderStatus.ASSIGNED);

        return Optional.of(order);
    }

    public void markOrderOutForDelivery(String orderId) {
        Order order = findOrder(orderId);

        if (!order.getOrderStatus().canTransitionTo(OrderStatus.OUT_FOR_DELIVERY)) {
            throw new IllegalOrderTransitionException(order.getOrderStatus(), OrderStatus.OUT_FOR_DELIVERY);
        }

        order.setOutForDeliveryAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void completeOrder(String orderId) {
        Order order = findOrder(orderId);

        if (!order.getOrderStatus().canTransitionTo(OrderStatus.DELIVERED))
            throw new IllegalOrderTransitionException(order.getOrderStatus(), OrderStatus.DELIVERED);

        Rider rider = order.getAssignedRider();

        rider.updateRiderStatus(RiderStatus.AVAILABLE);
        rider.clearActiveOrder();
        rider.completeOrder();

        order.setDeliveredAt(LocalDateTime.now());

        order.getCustomer().incrementCompletedOrders();

        order.setOrderStatus(OrderStatus.DELIVERED);
    }
}
