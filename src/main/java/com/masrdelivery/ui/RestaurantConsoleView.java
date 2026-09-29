package com.masrdelivery.ui;

import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderStatus;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.MenuItemCategory;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.restaurant.common.MenuItemType;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.service.OrderService;
import com.masrdelivery.service.RestaurantService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestaurantConsoleView {
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final InputReader input;

    public RestaurantConsoleView(
            RestaurantService restaurantService,
            OrderService orderService,
            InputReader input
    ) {
        if (restaurantService == null) {
            throw new NullEntityException("RestaurantService");
        }
        if (orderService == null) {
            throw new NullEntityException("OrderService");
        }
        if (input == null) {
            throw new NullEntityException("InputReader");
        }
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.input = input;
    }

    public void run() {
        System.out.println("========== Restaurant Selection ==========");
        List<Restaurant> restaurants = restaurantService.listRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found on the platform.");
            return;
        }

        System.out.println("Available Restaurants:");
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            if (restaurant == null) {
                break;
            }
            String status = restaurant.isOpen() ? "OPEN" : "CLOSED";
            System.out.printf("  %d. %s [%s] - Status: %s%n", i + 1, restaurant.getName(), restaurant.getDistrict(), status);
        }

        int selection = input.readIntInRange("Select restaurant: (0 to go to home menu)", 0, restaurants.size());
        if (selection == 0) {
            return;
        }
        Restaurant selectedRestaurant = restaurants.get(selection - 1);

        boolean running = true;
        while (running) {
            System.out.printf("%n========================================%n");
            System.out.printf("Restaurant Area: %s [%s] - %s%n",
                    selectedRestaurant.getName(),
                    selectedRestaurant.getDistrict(),
                    selectedRestaurant.isOpen() ? "OPEN" : "CLOSED");
            System.out.println("========================================");
            System.out.println("1. View Pending / Active Orders");
            System.out.println("2. Accept or Reject Order");
            System.out.println("3. Advance Order Status (Preparing -> Ready)");
            System.out.println("4. Toggle Item Availability");
            System.out.println("5. Adjust Stock for an Item");
            System.out.println("6. Add Menu Item");
            System.out.println("7. Remove Menu Item");
            System.out.println("8. Toggle Restaurant Open/Closed Status");
            System.out.println("9. View Today's Orders & Revenue");
            System.out.println("0. Return to Main Menu");

            try {
                int choice = input.readIntInRange("Choose an option: ", 0, 9);
                switch (choice) {
                    case 1 -> handleViewOrders(selectedRestaurant);
                    case 2 -> handleAcceptOrRejectOrder(selectedRestaurant);
                    case 3 -> handleAdvanceOrderStatus(selectedRestaurant);
                    case 4 -> handleToggleAvailability(selectedRestaurant);
                    case 5 -> handleAdjustStock(selectedRestaurant);
                    case 6 -> handleAddMenuItem(selectedRestaurant);
                    case 7 -> handleRemoveMenuItem(selectedRestaurant);
                    case 8 -> handleToggleStoreStatus(selectedRestaurant);
                    case 9 -> handleViewTodaysOrdersAndRevenue(selectedRestaurant);
                    case 0 -> running = false;
                }
            } catch (PlatformException e) {
                System.out.println("\n[PLATFORM_ERROR] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            }
        }
    }

    private void handleViewOrders(Restaurant restaurant) {
        List<Order> orders = getOrdersForRestaurant(restaurant);
        if (orders.isEmpty()) {
            System.out.println("\nNo orders found for this restaurant.");
            return;
        }

        System.out.printf("%n--- Orders for %s ---%n", restaurant.getName());
        for (Order order : orders) {
            if (order == null) {
                break;
            }
            System.out.printf("Order #%s | Status: %s | Customer: %s | Items: %d | Total: %.2f EGP%n",
                    order.getId(), order.getOrderStatus(), order.getCustomer().getName(), order.getOrderItems().size(), order.getTotal());
        }
    }

    private void handleAcceptOrRejectOrder(Restaurant restaurant) {
        String orderId = input.readNonEmptyString("\nEnter Order ID: ");
        Order order = orderService.findOrder(orderId);

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            System.out.println("[ERROR] This order belongs to a different restaurant.");
            return;
        }

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            System.out.printf("[ERROR] Only orders in PLACED status can be accepted or rejected (Current: %s).%n",
                    order.getOrderStatus());
            return;
        }

        System.out.println("1. Accept Order");
        System.out.println("2. Reject Order (Cancel & Refund)");
        int action = input.readIntInRange("Choose action (1-2): ", 1, 2);

        if (action == 1) {
            orderService.updateStatus(orderId, OrderStatus.ACCEPTED);
            System.out.printf("Order %s accepted! Ready to move to PREPARING.%n", orderId);
        } else {
            orderService.cancelOrder(orderId);
            System.out.printf("Order %s has been rejected and cancelled.%n", orderId);
        }
    }

    private void handleAdvanceOrderStatus(Restaurant restaurant) {
        String orderId = input.readNonEmptyString("\nEnter Order ID to advance: ");
        Order order = orderService.findOrder(orderId);

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            System.out.println("[ERROR] This order belongs to a different restaurant.");
            return;
        }

        switch (order.getOrderStatus()) {
            case ACCEPTED -> {
                orderService.updateStatus(orderId, OrderStatus.PREPARING);
                System.out.printf("Order %s is now PREPARING in the kitchen.%n", orderId);
            }
            case PREPARING -> {
                if (!order.isPaid()) {
                    System.out.println("[ERROR] Order must be paid before marking as ready.");
                    return;
                }
                orderService.markOrderReady(orderId);
                System.out.printf("Order %s is marked READY and entered the priority dispatch queue!%n", orderId);
            }
            default -> System.out.printf("[INFO] Order %s is currently in status %s. Kitchen actions not applicable.%n",
                    orderId, order.getOrderStatus());
        }
    }

    private void handleToggleAvailability(Restaurant restaurant) {
        MenuItem item = selectMenuItem(restaurant);
        if (item == null) return;

        boolean newStatus = !item.isAvailable();
        restaurantService.updateMenuItemAvailability(restaurant.getId(), item.getId(), newStatus);
        System.out.printf("Item '%s' marked as %s.%n", item.getName(), newStatus ? "AVAILABLE" : "UNAVAILABLE");
    }

    private void handleAdjustStock(Restaurant restaurant) {
        MenuItem item = selectMenuItem(restaurant);
        if (item == null) return;

        System.out.printf("Current stock for '%s': %.2f%n", item.getName(), item.getStock());
        System.out.println("1. Add stock (Restock)");
        System.out.println("2. Deduct stock");
        int mode = input.readIntInRange("Choose option (1-2): ", 1, 2);
        BigDecimal amount = input.readPositiveBigDecimal("Enter amount: ");

        if (mode == 1) {
            item.restock(amount);
            System.out.printf("Restocked! New stock: %.2f%n", item.getStock());
        } else {
            if (amount.compareTo(item.getStock()) > 0) {
                System.out.println("[ERROR] Cannot deduct more than available stock.");
                return;
            }
            item.deductStock(amount);
            System.out.printf("Deducted! New stock: %.2f%n", item.getStock());
        }
    }

    private void handleAddMenuItem(Restaurant restaurant) {
        System.out.println("\n--- Add Menu Item ---");
        MenuItemType type = input.readEnum("Select Item Type:", MenuItemType.class);
        String name = input.readNonEmptyString("Item Name: ");
        MenuItemCategory category = input.readEnum("Select Category:", MenuItemCategory.class);
        int prepTime = input.readPositiveInt("Preparation time in minutes: ");
        BigDecimal stock = input.readPositiveBigDecimal("Initial stock: ");
        BigDecimal price = input.readPositiveBigDecimal(type == MenuItemType.WEIGHTED ? "Price per KG: " : "Price: ");

        List<MenuItem> bundleItems = new ArrayList<>();
        if (type == MenuItemType.COMBO) {
            System.out.println("Select items to bundle into this combo:");
            boolean bundling = true;
            while (bundling) {
                MenuItem bundledItem = selectMenuItem(restaurant);
                if (bundledItem != null) {
                    bundleItems.add(bundledItem);
                }
                char more = input.readNonEmptyCharOutOfPool("Add another item to bundle? (y/n): ", new char[] {'y', 'n', 'Y', 'N'});
                if (more == 'n' || more == 'N') {
                    bundling = false;
                }
            }
        }

        MenuItem created = restaurantService.createMenuItem(
                restaurant.getId(),
                type,
                name,
                category,
                prepTime,
                true,
                stock,
                price,
                bundleItems
        );

        System.out.printf("Menu item '%s' [%s] successfully created and added to menu!%n",
                created.getName(), created.getId());
    }

    private void handleRemoveMenuItem(Restaurant restaurant) {
        MenuItem item = selectMenuItem(restaurant);
        if (item == null) return;

        restaurantService.removeMenuItem(restaurant.getId(), item);
        System.out.printf("Menu item '%s' [%s] removed from the menu.%n", item.getName(), item.getId());
    }

    private void handleToggleStoreStatus(Restaurant restaurant) {
        boolean newState = !restaurant.isOpen();
        restaurantService.updateRestaurantStatus(restaurant.getId(), newState);
        System.out.printf("Restaurant %s is now %s.%n", restaurant.getName(), newState ? "OPEN" : "CLOSED");
    }

    private void handleViewTodaysOrdersAndRevenue(Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        List<Order> todaysOrders = orderService.listOrders().stream()
                .filter(o -> o.getRestaurant().getId().equals(restaurant.getId()))
                .filter(o -> o.getPlacedAt().toLocalDate().equals(today))
                .toList();

        BigDecimal todaysRevenue = todaysOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("%n=== Today's Summary for %s (%s) ===%n", restaurant.getName(), today);
        System.out.printf("Total Orders Placed Today: %d%n", todaysOrders.size());
        System.out.printf("Total Delivered Revenue:   %.2f EGP%n", todaysRevenue);

        System.out.println("\nToday's Orders:");
        for (Order o : todaysOrders) {
            System.out.printf("  #%s | Placed: %s | Status: %s | Total: %.2f EGP%n",
                    o.getId(), o.getPlacedAt().toLocalTime().withNano(0), o.getOrderStatus(), o.getTotal());
        }
    }

    private List<Order> getOrdersForRestaurant(Restaurant restaurant) {
        return orderService.listOrders().stream()
                .filter(o -> o.getRestaurant().getId().equals(restaurant.getId()))
                .toList();
    }

    private MenuItem selectMenuItem(Restaurant restaurant) {
        Map<String, MenuItem> menu = restaurant.getMenu();
        if (menu.isEmpty()) {
            System.out.println("The restaurant menu is currently empty.");
            return null;
        }

        List<MenuItem> items = new ArrayList<>(menu.values());
        System.out.println("\nSelect Menu Item:");
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            if (item == null) {
                break;
            }
            System.out.printf("  %d. %s [%s] (Stock: %.2f) - %s%n",
                    i + 1, item.getName(), item.getId(), item.getStock(), item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE");
        }

        int choice = input.readIntInRange("Choose item: ", 1, items.size());
        return items.get(choice - 1);
    }
}
