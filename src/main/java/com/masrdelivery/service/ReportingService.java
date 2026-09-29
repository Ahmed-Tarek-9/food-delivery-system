package com.masrdelivery.service;

import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderItem;
import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.repository.*;
import com.masrdelivery.service.dto.CustomerOrderHistory;
import com.masrdelivery.service.dto.RiderDeliveryStats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportingService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final RestaurantRepository restaurantRepository;
    private final RiderRepository riderRepository;


    public ReportingService(
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            PromotionRepository promotionRepository,
            RestaurantRepository restaurantRepository,
            RiderRepository riderRepository
    ) {

        if (customerRepository == null) {
            throw new NullEntityException("CustomerRepository");
        }

        if (orderRepository == null) {
            throw new NullEntityException("OrderRepository");
        }

        if (promotionRepository == null) {
            throw new NullEntityException("PromotionRepository");
        }

        if (restaurantRepository == null) {
            throw new NullEntityException("RestaurantRepository");
        }

        if (riderRepository == null) {
            throw new NullEntityException("RiderRepository");
        }

        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.promotionRepository = promotionRepository;
        this.restaurantRepository = restaurantRepository;
        this.riderRepository = riderRepository;
    }

    public BigDecimal calculateRevenueForDateRange(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new NullEntityException("Date");
        }

        return orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> order.getPlacedAt().toLocalDate().isAfter(startDate.minusDays(1)))
                .filter(order -> order.getPlacedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateRestaurantRevenueForDateRange(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        if (restaurant == null) {
            throw new NullEntityException("Restaurant");
        }
        if (startDate == null || endDate == null) {
            throw new NullEntityException("Date");
        }

        return orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> order.getRestaurant().equals(restaurant))
                .filter(order -> order.getPlacedAt().toLocalDate().isAfter(startDate))
                .filter(order -> order.getPlacedAt().toLocalDate().isBefore(endDate))
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Map.Entry<Restaurant, BigDecimal>> getTopFiveRestaurantsByMonth(YearMonth yearMonth) {
        if (yearMonth == null) {
            throw new NullEntityException("YearMonth");
        }
        return orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> YearMonth.from(order.getPlacedAt()).equals(yearMonth))
                .collect(
                        Collectors.groupingBy(
                                Order::getRestaurant,
                                Collectors.mapping(
                                        Order::getTotal,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        )
                )
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<Restaurant, BigDecimal>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        entry -> entry.getKey().getName()
                                )
                )
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<District, BigDecimal> getAverageOrderValuePerDistrict () {

        return orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .collect(
                        Collectors.groupingBy(
                                order -> order.getDeliveryAddress().district(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) return BigDecimal.ZERO;
                                            BigDecimal sum = list.stream()
                                                    .map(Order::getTotal)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            return sum.divide(new BigDecimal(list.size()), 2, RoundingMode.HALF_UP);
                                        }
                                )
                        )
                );
    }

    public List<Restaurant> getHighPerformanceRestaurants() {
        Map<Restaurant, Long> completedOrdersCount = orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .collect(
                        Collectors.groupingBy(
                                Order::getRestaurant,
                                Collectors.counting()
                        )
                );

        return completedOrdersCount
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getRating().compareTo(new BigDecimal("4.5")) >= 0)
                .filter(entry -> entry.getValue() >= 20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<OrderStatus, Long> getOrderCountByStatus() {
        return orderRepository
                .findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Order::getOrderStatus,
                                Collectors.counting()
                        )
                );
    }

    public List<RiderDeliveryStats> getRiderPerformanceStats() {
        Map<Rider, List<Order>> riderOrders = orderRepository
                .findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> order.getAssignedRider() != null)
                .filter(order -> order.getOutForDeliveryAt() != null)
                .filter(order -> order.getDeliveredAt() != null)
                .collect(
                        Collectors.groupingBy(Order::getAssignedRider)
                );

        return riderRepository
                .findAll()
                .stream()
                .map(
                        rider -> {
                            List<Order> orders = riderOrders.getOrDefault(rider, Collections.emptyList());
                            long count = orders.size();

                            double avgMinutes = orders.stream()
                                    .mapToLong(
                                            order ->
                                                    Duration.between(
                                                            order.getOutForDeliveryAt(),
                                                            order.getDeliveredAt()
                                                    ).toMinutes()
                                    ).average()
                                    .orElse(0.0);
                            return new RiderDeliveryStats(rider, count, avgMinutes);
                        }
                )
                .sorted(
                        Comparator.comparingLong(
                                RiderDeliveryStats::completedDeliveries
                        ).reversed()
                )
                .collect(Collectors.toList());
    }

    public Optional<MenuItem> getMostFrequentOrderedMenuItem() {
        return orderRepository
                .findAll()
                .stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(
                        Collectors.groupingBy(
                                OrderItem::getMenuItem,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        OrderItem::getQuantity,
                                        BigDecimal::add
                                )
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.<MenuItem, BigDecimal>comparingByValue())
                .map(Map.Entry::getKey);
    }

    public CustomerOrderHistory getCustomerOrderHistory(Customer customer) {

        if (customer == null) {
            throw new NullEntityException("Customer");
        }

        List<Order> customerOrders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getCustomer().equals(customer))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();

        BigDecimal totalSpent = customerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerOrderHistory(customer, customerOrders, totalSpent);
    }

    public OptionalInt getPeakOrderingHour() {
        return orderRepository
                .findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                order ->
                                        order.getPlacedAt().getHour(),
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.<Integer, Long>comparingByValue())
                .map(entry -> OptionalInt.of(entry.getKey()))
                .orElseGet(OptionalInt::empty);
    }

    public List<Customer> getInactiveCustomers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        Set<Customer> activeCustomers = orderRepository.findAll()
                .stream()
                .filter(order -> order.getPlacedAt().isAfter(thirtyDaysAgo))
                .map(Order::getCustomer)
                .collect(Collectors.toSet());

        return customerRepository
                .findAll()
                .stream()
                .filter(customer -> !activeCustomers.contains(customer))
                .toList();
    }

}
