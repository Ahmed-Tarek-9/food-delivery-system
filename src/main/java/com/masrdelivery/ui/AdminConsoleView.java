package com.masrdelivery.ui;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.customer.LoyaltyTier;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.promotion.common.PromotionConditionType;
import com.masrdelivery.domain.promotion.common.PromotionType;
import com.masrdelivery.domain.promotion.condition.*;
import com.masrdelivery.domain.promotion.factory.PromotionConditionConfig;
import com.masrdelivery.domain.promotion.factory.PromotionConditionFactory;
import com.masrdelivery.domain.promotion.factory.PromotionConfig;
import com.masrdelivery.domain.restaurant.Cuisine;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.domain.rider.VehicleType;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.repository.CustomerRepository;
import com.masrdelivery.repository.RiderRepository;
import com.masrdelivery.service.*;
import com.masrdelivery.service.dto.CustomerOrderHistory;
import com.masrdelivery.service.dto.RiderDeliveryStats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class AdminConsoleView {

    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final RiderService riderService;
    private final OrderService orderService;
    private final PromotionService promotionService;
    private final ReportingService reportingService;
    private final CustomerRepository customerRepository;
    private final RiderRepository riderRepository;
    private final InputReader input;

    public AdminConsoleView(
            CustomerService customerService,
            RestaurantService restaurantService,
            RiderService riderService,
            OrderService orderService,
            PromotionService promotionService,
            ReportingService reportingService,
            CustomerRepository customerRepository,
            RiderRepository riderRepository,
            InputReader input
    ) {
        if (customerService == null) {
            throw new NullEntityException("CustomerService");
        }
        if (restaurantService == null) {
            throw new NullEntityException("RestaurantService");
        }
        if (riderService == null) {
            throw new NullEntityException("RiderService");
        }
        if (orderService == null) {
            throw new NullEntityException("OrderService");
        }
        if (promotionService == null) {
            throw new NullEntityException("PromotionService");
        }
        if (reportingService == null) {
            throw new NullEntityException("ReportingService");
        }
        if (customerRepository == null) {
            throw new NullEntityException("CustomerRepository");
        }
        if (riderRepository == null) {
            throw new NullEntityException("RiderRepository");
        }
        if (input == null) {
            throw new NullEntityException("InputReader");
        }

        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.riderService = riderService;
        this.orderService = orderService;
        this.promotionService = promotionService;
        this.reportingService = reportingService;
        this.customerRepository = customerRepository;
        this.riderRepository = riderRepository;
        this.input = input;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("========================================");
            System.out.println("          PLATFORM ADMIN AREA           ");
            System.out.println("========================================");
            System.out.println("--- Platform Management ---");
            System.out.println(" 1. Register New Customer");
            System.out.println(" 2. Register New Restaurant");
            System.out.println(" 3. Onboard New Rider");
            System.out.println(" 4. Create Promotion Code");
            System.out.println(" 5. List All Entities (Customers, Restaurants, Riders, Promos)");
            System.out.println(" 6. Trigger Priority Order Dispatch (Next Ready Order)");
            System.out.println("--- Analytics & Reports ---");
            System.out.println(" 7. Report 1: Revenue for Date Range");
            System.out.println(" 8. Report 2: Top 5 Restaurants by Monthly Revenue");
            System.out.println(" 9. Report 3: Average Order Value per District");
            System.out.println("10. Report 4: High-Performing Restaurants (>4.5 Rating, >=20 Orders)");
            System.out.println("11. Report 5: Order Count by Status");
            System.out.println("12. Report 6: Rider Performance & Delivery Transit Times");
            System.out.println("13. Report 7: Most Frequently Ordered Menu Item");
            System.out.println("14. Report 8: Customer Lifetime History & Total Spend");
            System.out.println("15. Report 9: Peak Ordering Hour of the Day");
            System.out.println("16. Report 10: Inactive Customers (Last 30 Days)");
            System.out.println(" 0. Return to Main Menu");

            try {
                int choice = input.readIntInRange("Select an option (0-16): ", 0, 16);
                switch (choice) {
                    case 1 -> handleRegisterCustomer();
                    case 2 -> handleRegisterRestaurant();
                    case 3 -> handleOnboardRider();
                    case 4 -> handleCreatePromotion();
                    case 5 -> handleListAllEntities();
                    case 6 -> handleTriggerManualDispatch();
                    case 7 -> handleReportRevenueDateRange();
                    case 8 -> handleReportTopRestaurants();
                    case 9 -> handleReportAovPerDistrict();
                    case 10 -> handleReportHighPerformingRestaurants();
                    case 11 -> handleReportOrdersByStatus();
                    case 12 -> handleReportRiderPerformance();
                    case 13 -> handleReportMostOrderedItem();
                    case 14 -> handleReportCustomerSpend();
                    case 15 -> handleReportPeakHour();
                    case 16 -> handleReportInactiveCustomers();
                    case 0 -> running = false;
                }
            } catch (PlatformException e) {
                System.out.println("\n[PLATFORM_ERROR] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            }
        }
    }

    private void handleRegisterCustomer() {
        System.out.println("=== Register Customer ===");
        String name = input.readNonEmptyString("Customer Full Name: ");
        String phone = input.readNonEmptyString("Phone: ");

        District district = input.readEnum("Select Address District:", District.class);
        String details = input.readNonEmptyString("Street, Building, Apartment details: ");
        Address address = new Address(district, details);

        Customer customer = customerService.createCustomer(name, phone, address);
        System.out.printf("Customer '%s' registered successfully with ID: %s%n", customer.getName(), customer.getId());
    }

    private void handleRegisterRestaurant() {
        System.out.println("=== Register Restaurant ===");
        String name = input.readNonEmptyString("Restaurant Name: ");
        District district = input.readEnum("Select Restaurant District:", District.class);

        Set<Cuisine> cuisines = new HashSet<>();
        boolean addingCuisines = true;
        while (addingCuisines) {
            cuisines.add(input.readEnum("Select Cuisine:", Cuisine.class));
            char more = input.readNonEmptyCharOutOfPool("Add another cuisine category? (y/n): ", new char[] {'y', 'n', 'Y', 'N'});
            if (more == 'n' || more == 'N') {
                addingCuisines = false;
            }
        }

        BigDecimal rating = input.readBigDecimalInRange("Restaurant Rating (0.0 - 5.0): ",
                new BigDecimal("0.0"),
                new BigDecimal("5.0")
        );

        Restaurant restaurant = restaurantService.createRestaurant(name, district, cuisines, rating);
        System.out.printf("Restaurant '%s' registered successfully with ID: %s%n", restaurant.getName(), restaurant.getId());
    }

    private void handleOnboardRider() {
        System.out.println("=== Onboard Rider ===");
        String name = input.readNonEmptyString("Rider Name: ");
        String phone = input.readNonEmptyString("Phone: ");
        VehicleType vehicle = input.readEnum("Select Vehicle Type:", VehicleType.class);
        District startingDistrict = input.readEnum("Select Starting District:", District.class);

        Rider rider = riderService.createRider(name, phone, vehicle, startingDistrict);
        System.out.printf("Rider '%s' onboarded successfully with ID: %s%n", rider.getName(), rider.getId());
    }

    private void handleCreatePromotion() {
        System.out.println("=== Create Promotion ===");
        PromotionType type = input.readEnum("Select Promotion Type:", PromotionType.class);

        String code = input.readNonEmptyString("Promotion Code (e.g. SAVE20): ").toUpperCase();
        String desc = input.readNonEmptyString("Description: ");

        PromotionConfig config = switch (type) {
            case PERCENTAGE -> {
                BigDecimal percentage = input.readPositiveBigDecimal("Percentage (e.g. 15 for 15%): ");
                BigDecimal maxDiscount = input.readPositiveBigDecimal("Enter Maximum Discount Cap in EGP: ");
                yield PromotionConfig.percentage(percentage, maxDiscount);
            }
            case FIXED_AMOUNT -> {
                BigDecimal fixedAmount = input.readPositiveBigDecimal("Fixed Discount Amount (EGP): ");
                yield PromotionConfig.fixedAmount(fixedAmount);
            }
            case FREE_DELIVERY -> PromotionConfig.freeDelivery();

        };

        List<PromotionCondition> conditions = new ArrayList<>();
        char addCondition = input.readNonEmptyCharOutOfPool("Do you want to add a condition? (y/n): ", new char[] {'y', 'n', 'Y', 'N'});

        if (addCondition == 'y' || addCondition == 'Y') {
            boolean addingConditions = true;
            while (addingConditions) {

                PromotionConditionType promotionConditionType = input.readEnum("Select Promotion Condition:", PromotionConditionType.class);
                PromotionConditionConfig conditionConfig = null;

                switch (promotionConditionType) {
                    case MINIMUM_ORDER_VALUE -> {
                        BigDecimal minOrder = input.readPositiveBigDecimal("Enter Minimum Order Value (EGP): ");
                        conditionConfig = PromotionConditionConfig.minimumOrderValue(minOrder);
                    }
                    case EXPIRY_DATE -> {
                        int validDays = input.readPositiveInt("Valid for how many days from now? ");
                        LocalDate expiryDate = LocalDate.now().plusDays(validDays);
                        conditionConfig = PromotionConditionConfig.expiryDate(expiryDate);
                    }
                    case FIRST_ORDER -> {
                        conditionConfig = PromotionConditionConfig.firstOrder();
                    }
                    case LOYALTY_TIER -> {
                        LoyaltyTier tier = input.readEnum("Select required Loyalty Tier:", LoyaltyTier.class);
                        conditionConfig = PromotionConditionConfig.loyaltyTier(tier);
                    }
                    case DISTRICT -> {
                        District district = input.readEnum("Select applicable District:", District.class);
                        conditionConfig = PromotionConditionConfig.district(district);
                    }
                }
                conditions.add(PromotionConditionFactory.create(conditionConfig));
                System.out.println("Condition added successfully!");
                char more = input.readNonEmptyCharOutOfPool("Add another condition? (y/n): ", new char[] {'y', 'n', 'Y', 'N'});
                if (more == 'n' || more == 'N') {
                    addingConditions = false;
                }
            }
        }

        promotionService.createPromotion(code, desc, config, conditions);
        System.out.printf("%nPromotion '%s' created successfully with %d condition(s)!%n", code, conditions.size());
    }

    private void handleListAllEntities() {
        System.out.println("=== PLATFORM REGISTRY OVERVIEW ===");
        System.out.printf("Customers:   %d registered%n", customerService.listCustomers().size());
        System.out.printf("Restaurants: %d registered%n", restaurantService.listRestaurants().size());
        System.out.printf("Riders:      %d registered%n", riderService.listRiders().size());
        System.out.printf("Promotions:  %d active%n", promotionService.listPromotions().size());
        System.out.printf("Orders:      %d total placed%n", orderService.listOrders().size());
    }

    private void handleTriggerManualDispatch() {
        System.out.println("=== Trigger Dispatch Queue ===");
        Optional<Order> dispatched = orderService.dispatchNextReadyOrder();
        if (dispatched.isPresent()) {
            Order order = dispatched.get();
            System.out.printf("Order %s from %s was dispatched to Rider %s!%n",
                    order.getId(), order.getRestaurant().getName(), order.getAssignedRider().getName());
        } else {
            System.out.println("No ready orders currently waiting in the dispatch queue.");
        }
    }

    private void handleReportRevenueDateRange() {
        System.out.println("=== Report 1: Revenue by Date Range ===");
        int daysBack = input.readPositiveInt("Look back how many days from today? ");
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        LocalDate endDate = LocalDate.now();

        BigDecimal revenue = reportingService.calculateRevenueForDateRange(startDate, endDate);
        System.out.printf("Total Delivered Revenue between %s and %s: %.2f EGP%n", startDate, endDate, revenue);
    }

    private void handleReportTopRestaurants() {
        System.out.println("=== Report 2: Top 5 Restaurants by Monthly Revenue ===");
        YearMonth currentMonth = YearMonth.now();
        List<Map.Entry<Restaurant, BigDecimal>> top5 = reportingService.getTopFiveRestaurantsByMonth(currentMonth);

        System.out.printf("Top 5 Restaurants for %s:%n", currentMonth);
        if (top5.isEmpty()) {
            System.out.println("No delivered orders found for this month.");
            return;
        }

        int rank = 1;
        for (Map.Entry<Restaurant, BigDecimal> entry : top5) {
            System.out.printf("  %d. %-22s [%-12s] Revenue: %.2f EGP%n",
                    rank++, entry.getKey().getName(), entry.getKey().getDistrict(), entry.getValue());
        }
    }

    private void handleReportAovPerDistrict() {
        System.out.println("=== Report 3: Average Order Value per District ===");
        Map<District, BigDecimal> aovMap = reportingService.getAverageOrderValuePerDistrict();

        if (aovMap.isEmpty()) {
            System.out.println("No completed orders on record.");
            return;
        }

        aovMap.forEach((district, aov) ->
                System.out.printf("  %-16s: %.2f EGP%n", district, aov));
    }

    private void handleReportHighPerformingRestaurants() {
        System.out.println("=== Report 4: High Performing Restaurants (>4.5 Rating, >=20 Orders) ===");
        List<Restaurant> highPerformers = reportingService.getHighPerformanceRestaurants();

        if (highPerformers.isEmpty()) {
            System.out.println("No restaurants currently qualify (>4.5 rating and at least 20 delivered orders).");
            return;
        }

        for (Restaurant restaurant : highPerformers) {
            System.out.printf("  * %s [%s] - Rating: %.1f%n", restaurant.getName(), restaurant.getDistrict(), restaurant.getRating());
        }
    }

    private void handleReportOrdersByStatus() {
        System.out.println("== Report 5: Orders Grouped by Status ===");
        Map<OrderStatus, Long> counts = reportingService.getOrderCountByStatus();

        if (counts.isEmpty()) {
            System.out.println("No orders have been placed in the system.");
            return;
        }

        for (OrderStatus status : OrderStatus.values()) {
            long count = counts.getOrDefault(status, 0L);
            System.out.printf("  %-18s: %d%n", status, count);
        }
    }

    private void handleReportRiderPerformance() {
        System.out.println("=== Report 6: Rider Performance Stats ===");
        List<RiderDeliveryStats> stats = reportingService.getRiderPerformanceStats();

        if (stats.isEmpty()) {
            System.out.println("No riders registered.");
            return;
        }

        System.out.printf("%-18s | %-12s | %-10s | %-12s%n", "Rider Name", "District", "Deliveries", "Avg Transit (min)");
        System.out.println("---------------------------------------------------------------");
        for (RiderDeliveryStats rds : stats) {
            System.out.printf("%-18s | %-12s | %-10d | %-12.1f%n",
                    rds.rider().getName(), rds.rider().getCurrentDistrict(), rds.completedDeliveries(), rds.averageDeliveryMinutes());
        }
    }

    private void handleReportMostOrderedItem() {
        System.out.println("=== Report 7: Most Frequently Ordered Menu Item ===");
        Optional<MenuItem> itemOpt = reportingService.getMostFrequentOrderedMenuItem();

        if (itemOpt.isPresent()) {
            MenuItem item = itemOpt.get();
            System.out.printf("Most Ordered Item: %s [%s] (Category: %s)%n",
                    item.getName(), item.getId(), item.getCategory());
        } else {
            System.out.println("No orders placed yet.");
        }
    }

    private void handleReportCustomerSpend() {
        System.out.println("=== Report 8: Customer Spend History ===");
        List<Customer> customers = customerService.listCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }

        for (int i = 0; i < customers.size(); i++) {
            System.out.printf("  %d. %s (%s)%n", i + 1, customers.get(i).getName(), customers.get(i).getId());
        }
        int selectedIndex = input.readIntInRange("Select customer: ", 1, customers.size());
        Customer customer = customers.get(selectedIndex - 1);

        CustomerOrderHistory history = reportingService.getCustomerOrderHistory(customer);
        System.out.printf("%nCustomer: %s | Total Spent: %.2f EGP | Total Orders: %d%n",
                customer.getName(), history.totalSpent(), history.orders().size());

        for (Order order : history.orders()) {
            System.out.printf("  Order #%s | Date: %s | Status: %s | Total: %.2f EGP%n",
                    order.getId(), order.getPlacedAt().toLocalDate(), order.getOrderStatus(), order.getTotal());
        }
    }

    private void handleReportPeakHour() {
        System.out.println("=== Report 9: Peak Ordering Hour ===");
        OptionalInt peak = reportingService.getPeakOrderingHour();

        if (peak.isPresent()) {
            int hour = peak.getAsInt();
            System.out.printf("Peak Ordering Hour across all orders is %02d:00 - %02d:59%n", hour, hour);
        } else {
            System.out.println("No orders placed yet.");
        }
    }

    private void handleReportInactiveCustomers() {
        System.out.println("=== Report 10: Inactive Customers (Last 30 Days) ===");
        List<Customer> inactives = reportingService.getInactiveCustomers();

        if (inactives.isEmpty()) {
            System.out.println("Every registered customer has placed at least one order in the last 30 days!");
        } else {
            System.out.printf("Found %d inactive customer(s):%n", inactives.size());
            for (Customer c : inactives) {
                System.out.printf("  * %s (%s) - Tier: %s%n", c.getName(), c.getMobileNumber(), c.getLoyaltyTier());
            }
        }
    }
}
