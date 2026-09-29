package com.masrdelivery.ui;

import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.rider.Rider;
import com.masrdelivery.domain.rider.RiderStatus;
import com.masrdelivery.exception.NoRiderAvailableException;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.service.OrderService;
import com.masrdelivery.service.ReportingService;
import com.masrdelivery.service.RiderService;
import com.masrdelivery.service.dto.RiderDeliveryStats;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class RiderConsoleView {

    private final RiderService riderService;
    private final OrderService orderService;
    private final ReportingService reportingService;
    private final InputReader input;

    public RiderConsoleView(
            RiderService riderService,
            OrderService orderService,
            ReportingService reportingService,
            InputReader input
    ) {
        if (riderService == null) {
            throw new NullEntityException("RiderService");
        }
        if (orderService == null) {
            throw new NullEntityException("OrderService");
        }
        if (input == null) {
            throw new NullEntityException("InputReader");
        }
        if (reportingService == null) {
            throw new NullEntityException("ReportingService");
        }

        this.riderService = riderService;
        this.orderService = orderService;
        this.reportingService = reportingService;
        this.input = input;
    }

    public void run() {
        System.out.println("========== Rider Selection ==========");
        List<Rider> riders = riderService.listRiders();
        if (riders.isEmpty()) {
            System.out.println("No riders registered on the platform.");
            return;
        }

        System.out.println("Available Riders:");
        for (int i = 0; i < riders.size(); i++) {
            Rider rider = riders.get(i);
            if (rider == null) {
                break;
            }
            System.out.printf("  %d. %s (%s) - District: %s - Vehicle: %s - Status: %s%n",
                    i + 1, rider.getName(), rider.getId(), rider.getCurrentDistrict(), rider.getVehicleType(), rider.getRiderStatus());
        }

        int selection = input.readIntInRange("Select your rider profile: (0 to go to home menu)", 0, riders.size());
        if (selection == 0) {
            return;
        }
        Rider currentRider = riders.get(selection - 1);

        boolean running = true;
        while (running) {
            System.out.printf("%n========================================%n");
            System.out.printf("Rider Area: %s [%s] | District: %s | Status: %s%n",
                    currentRider.getName(),
                    currentRider.getVehicleType(),
                    currentRider.getCurrentDistrict(),
                    currentRider.getRiderStatus());
            System.out.println("========================================");
            System.out.println("1. View Active Assigned Order");
            System.out.println("2. Toggle Duty Status (AVAILABLE / OFF_DUTY)");
            System.out.println("3. Check & Claim Next Ready Order (Dispatch Queue)");
            System.out.println("4. Pick Up Order from Restaurant (OUT_FOR_DELIVERY)");
            System.out.println("5. Complete Delivery at Door (DELIVERED)");
            System.out.println("6. View My Completed Delivery History & Stats");
            System.out.println("0. Return to Main Menu");

            try {
                int choice = input.readIntInRange("Choose an option: ", 0, 6);
                switch (choice) {
                    case 1 -> handleViewActiveOrder(currentRider);
                    case 2 -> handleToggleDuty(currentRider);
                    case 3 -> handleClaimNextOrder(currentRider);
                    case 4 -> handlePickUpOrder(currentRider);
                    case 5 -> handleCompleteDelivery(currentRider);
                    case 6 -> handleViewHistory(currentRider);
                    case 0 -> running = false;
                }
            } catch (PlatformException e) {
                System.out.println("\n[PLATFORM_ERROR] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            }
        }
    }

    private void handleViewActiveOrder(Rider rider) {
        Order activeOrder = rider.getActiveOrder();
        if (activeOrder == null) {
            System.out.println("\nYou currently have no active assigned orders.");
            return;
        }

        System.out.printf("%n=== Active Delivery for %s ===%n", rider.getName());
        System.out.printf("Order ID:         %s%n", activeOrder.getId());
        System.out.printf("Status:           %s%n", activeOrder.getOrderStatus());
        System.out.printf("Restaurant:       %s (District: %s)%n",
                activeOrder.getRestaurant().getName(), activeOrder.getRestaurant().getDistrict());
        System.out.printf("Delivery To:      %s (%s)%n",
                activeOrder.getDeliveryAddress().district(), activeOrder.getDeliveryAddress().details());
        System.out.printf("Customer:         %s (%s)%n",
                activeOrder.getCustomer().getName(), activeOrder.getCustomer().getLoyaltyTier());
        System.out.printf("Total to Collect: %.2f EGP (Paid: %s)%n",
                activeOrder.getTotal(), activeOrder.isPaid() ? "YES" : "NO / CASH ON DELIVERY");
        if (activeOrder.getDeliveryNotes() != null && !activeOrder.getDeliveryNotes().isBlank()) {
            System.out.printf("Notes:            %s%n", activeOrder.getDeliveryNotes());
        }
    }

    private void handleToggleDuty(Rider rider) {
        if (rider.getRiderStatus() == RiderStatus.DELIVERING) {
            System.out.println("[ERROR] Cannot change duty status while currently delivering an order.");
            return;
        }

        if (rider.getRiderStatus() == RiderStatus.AVAILABLE) {
            rider.updateRiderStatus(RiderStatus.OFFLINE);
            System.out.printf("Rider %s is now OFF_DUTY.%n", rider.getName());
        } else {
            rider.updateRiderStatus(RiderStatus.AVAILABLE);
            System.out.printf("Rider %s is now AVAILABLE and on-duty in %s.%n",
                    rider.getName(), rider.getCurrentDistrict());
        }
    }

    private void handleClaimNextOrder(Rider rider) {
        if (rider.getRiderStatus() != RiderStatus.AVAILABLE) {
            System.out.printf("[ERROR] You must be in AVAILABLE status to take orders (Current: %s).%n",
                    rider.getRiderStatus());
            return;
        }

        try {
            Optional<Order> dispatched = orderService.dispatchNextReadyOrder();
            if (dispatched.isPresent()) {
                Order order = dispatched.get();
                System.out.printf("%nSuccess! Order %s from %s [%s] assigned to you!%n",
                        order.getId(), order.getRestaurant().getName(), order.getRestaurant().getDistrict());
            } else {
                System.out.println("\nNo orders are currently waiting in the ready dispatch queue.");
            }
        } catch (NoRiderAvailableException e) {
            System.out.println("[INFO] Orders are waiting, but no matching order for your district or conditions.");
        }
    }

    private void handlePickUpOrder(Rider rider) {
        Order activeOrder = rider.getActiveOrder();
        if (activeOrder == null) {
            System.out.println("[ERROR] You do not have an active order to pick up.");
            return;
        }

        if (activeOrder.getOrderStatus() != OrderStatus.ASSIGNED) {
            System.out.printf("[ERROR] Order is in status %s. Expected: ASSIGNED.%n",
                    activeOrder.getOrderStatus());
            return;
        }

        orderService.markOrderOutForDelivery(activeOrder.getId());
        System.out.printf("Order %s picked up from %s! Now OUT_FOR_DELIVERY to %s.%n",
                activeOrder.getId(),
                activeOrder.getRestaurant().getName(),
                activeOrder.getDeliveryAddress().district());
    }

    private void handleCompleteDelivery(Rider rider) {
        Order activeOrder = rider.getActiveOrder();
        if (activeOrder == null) {
            System.out.println("[ERROR] You do not have an active order to complete.");
            return;
        }

        if (activeOrder.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            System.out.printf("[ERROR] Order is in status %s. Expected: OUT_FOR_DELIVERY.%n",
                    activeOrder.getOrderStatus());
            return;
        }

        String orderId = activeOrder.getId();
        orderService.completeOrder(orderId);

        rider.updateCurrentDistrict(activeOrder.getDeliveryAddress().district());

        System.out.printf("Order %s successfully DELIVERED!%n", orderId);
        System.out.printf("Your status is now AVAILABLE in your new location: %s.%n",
                rider.getCurrentDistrict());
    }

    private void handleViewHistory(Rider rider) {

        RiderDeliveryStats stats = reportingService.getRiderPerformanceStats().stream()
                .filter(rds -> rds.rider().equals(rider))
                .findFirst()
                .orElse(new RiderDeliveryStats(rider, 0, 0.0));

        System.out.printf("%n=== Performance Summary for %s ===%n", rider.getName());
        System.out.printf("Total Completed Deliveries: %d%n", stats.completedDeliveries());
        System.out.printf("Average Delivery Duration (Transit Time): %.1f minutes%n", stats.averageDeliveryMinutes());

        List<Order> deliveries = orderService.listOrders().stream()
                .filter(order -> order.getAssignedRider() != null && order.getAssignedRider().equals(rider))
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .toList();

        if (deliveries.isEmpty()) {
            System.out.println("No completed deliveries on record yet.");
            return;
        }

        System.out.println("\nRecent Deliveries:");
        for (Order order : deliveries) {
            long transitMins = (order.getOutForDeliveryAt() != null && order.getDeliveredAt() != null)
                    ? Duration.between(order.getOutForDeliveryAt(), order.getDeliveredAt()).toMinutes()
                    : 0;
            System.out.printf("  #%s | From: %s | To: %s | Total: %.2f EGP | Transit: %d mins%n",
                    order.getId(),
                    order.getRestaurant().getDistrict(),
                    order.getDeliveryAddress().district(),
                    order.getTotal(),
                    transitMins);
        }
    }
}
