package com.masrdelivery.ui;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.order.Order;
import com.masrdelivery.domain.order.OrderItem;
import com.masrdelivery.domain.restaurant.Cuisine;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.exception.NullEntityException;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.service.*;
import com.masrdelivery.service.dto.CustomerOrderHistory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerConsoleView {

    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final ReportingService reportingService;
    private final InputReader input;

    public CustomerConsoleView(
            CustomerService customerService,
            RestaurantService restaurantService,
            OrderService orderService,
            ReportingService reportingService,
            InputReader input
    ) {
        if (customerService == null) {
            throw new NullEntityException("CustomerService");
        }
        if (restaurantService == null) {
            throw new NullEntityException("RestaurantService");
        }
        if (orderService == null) {
            throw new NullEntityException("OrderService");
        }
        if (reportingService == null) {
            throw new NullEntityException("ReportingService");
        }
        if (input == null) {
            throw new NullEntityException("InputReader");
        }

        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.reportingService = reportingService;
        this.input = input;
    }

    public void run() {
        System.out.println("========== Customer Selection ==========");
        List<Customer> customers = customerService.listCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found in the system.");
            return;
        }

        System.out.println("Available customers:");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer == null) {
                break;
            }
            System.out.printf(" %d. %s (%s) - Tier: %s - Wallet Balance: %.2f EGP%n",
                    i + 1,
                    customer.getName(),
                    customer.getId(),
                    customer.getLoyaltyTier(),
                    customer.getWalletBalance()
            );
        }

        int selection = input.readIntInRange("Select your customer profile: (0 to go to home menu)", 0, customers.size());
        if (selection == 0) {
            return;
        }
        Customer selectedCustomer = customers.get(selection - 1);

        boolean running = true;
        while (running) {
            System.out.printf("%n========================================%n");
            System.out.printf("Customer Area - Logged in as: %s (Tier: %s)%n", selectedCustomer.getName(), selectedCustomer.getLoyaltyTier());
            System.out.printf("%n========================================%n");
            System.out.println("1. Browser Restaurants (with filters)");
            System.out.println("2. Search Restaurants (free text)");
            System.out.println("3. View Restaurant Menu");
            System.out.println("4. Place Order");
            System.out.println("5. Pay Order from wallet");
            System.out.println("6. Track Order");
            System.out.println("7. Cancel Order");
            System.out.println("8. View Order History & Total Spent");
            System.out.println("9. View Recent Searches");
            System.out.println("10. Top Up Wallet");
            System.out.println("0. Return to Main Menu");

             try {
                 int choice = input.readIntInRange("Enter your choice: ", 0, 10);
                 switch (choice) {
                     case 1-> handleBrowseRestaurants();
                     case 2 -> handleSearchRestaurants(selectedCustomer);
                     case 3 -> handleViewRestaurantMenu();
                     case 4 -> handlePlaceOrder(selectedCustomer);
                     case 5 -> handlePayOrder(selectedCustomer);
                     case 6 -> handleTrackOrder();
                     case 7 -> handleCancelOrder(selectedCustomer);
                     case 8 -> handleViewOrderHistory(selectedCustomer);
                     case 9 -> handleViewRecentSearches(selectedCustomer);
                     case 10 -> handleTopUpWallet(selectedCustomer);
                     case 0 -> running = false;
                 }
             } catch (PlatformException e) {
                 System.out.println("\n[PLATFORM_ERROR] " + e.getMessage());
             } catch (RuntimeException e) {
                 System.out.println("\n[ERROR] " + e.getMessage());
             }
        }
    }

    private void handleBrowseRestaurants() {
        System.out.println("\n=== Browse Restaurants ===");
        char applyFilter = input.readNonEmptyCharOutOfPool("Do you want to apply filters? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
        List<Restaurant> list;

        if (applyFilter == 'y' || applyFilter == 'Y') {
            RestaurantFilterCriteria.Builder builder = new RestaurantFilterCriteria.Builder();

            char filterDistrict = input.readNonEmptyCharOutOfPool("Filter by district? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
            if (filterDistrict == 'y' || filterDistrict == 'Y') {
                builder.district(input.readEnum("Select District: ", District.class));
            }

            char filterCuisine = input.readNonEmptyCharOutOfPool("Filter by cuisine? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
            if (filterCuisine == 'y' || filterCuisine == 'Y') {
                builder.cuisine(input.readEnum("Select Cuisine: ", Cuisine.class));
            }

            char filterRating = input.readNonEmptyCharOutOfPool("Filter by rating? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
            if (filterRating == 'y' || filterRating == 'Y') {
                builder.minRating(input.readBigDecimalInRange("Enter minimum rating: ", BigDecimal.ZERO, BigDecimal.valueOf(5)));
            }

            char filterPrice = input.readNonEmptyCharOutOfPool("Filter by price? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
            if (filterPrice == 'y' || filterPrice == 'Y') {
                builder.maxPriceCieling(input.readPositiveBigDecimal("Enter maximum price: "));
            }

            list = restaurantService.searchRestaurants(builder.build());
        } else {
            list = restaurantService.findRestaurantsSortedByRating();
        }

        list = list.stream()
                .filter(Restaurant::isOpen)
                .collect(Collectors.toList());

        printRestaurantTable(list);
    }

    public void handleSearchRestaurants(Customer customer) {
        String query = input.readNonEmptyString("Enter search keyword: (restaurant name or cuisine): ");
        customerService.addSearch(customer.getId(), query);

        String lowerQuery = query.toLowerCase();
        List<Restaurant> matches = restaurantService.listRestaurants()
                .stream()
                .filter(
                        restaurant -> restaurant.getName().toLowerCase().contains(lowerQuery)
                        || restaurant.getCuisines().stream().anyMatch(
                                cuisine -> cuisine.name().toLowerCase().contains(lowerQuery)
                        )
                )
                .sorted(Comparator.comparing(Restaurant::getRating).reversed())
                .toList();

        if (matches.isEmpty()) {
            System.out.println("No restaurants found matching the search criteria.");
        } else {
            printRestaurantTable(matches);
        }
    }

    private void printRestaurantMenu(Map<String, MenuItem> menu) {
        for (MenuItem item : menu.values()) {
            if (item == null) {
                break;
            }
            String status = item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE";
            System.out.printf(" [%s] %s | Category: %s | Price: %.2fEGP | Prep: %d mins | Stock: %.1f | %s%n",
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.calculatePrice(BigDecimal.ONE),
                    item.getPreparationTimeMinutes(),
                    item.getStock(),
                    status
            );
        }
    }

    private void handlePlaceOrder(Customer customer) {
        Restaurant restaurant = selectRestaurantPrompt();
        if (restaurant == null) {
            return;
        }

        if (!restaurant.isOpen()) {
            System.out.println("[ERROR] This restaurant is currently closed.");
            return;
        }

        Map<String, MenuItem> menu = restaurant.getMenu();

        if(menu.isEmpty()) {
            System.out.println("This restaurant currently has no items on the menu");
            return;
        }

        Set<Address> addresses = customer.getAddresses();
        if (addresses.isEmpty()) {
            System.out.println("[ERROR] You have no addresses saved.");
            return;
        }

        List<Address> addressList = new ArrayList<>(addresses);
        System.out.println("\n=== Select Address ===");
        for (int i = 0; i < addressList.size(); i++) {
            Address addressIterator = addressList.get(i);
            if (addressIterator == null) {
                break;
            }
            System.out.printf(" %d. [%s] %s%n", i + 1, addressIterator.district(), addressIterator.details());
        }

        int addressChoice = input.readIntInRange("Choose address: ", 1, addresses.size());
        Address selectedAddress = addressList.get(addressChoice - 1);

        List<OrderItem> items = new ArrayList<>();
        boolean addingItems = true;

        printRestaurantMenu(menu);

        while (addingItems) {
            String itemId = input.readNonEmptyString("Enter the item ID to add: ");
            MenuItem item = restaurant.findMenuItem(itemId);

            BigDecimal quantity = input.readPositiveBigDecimal("Enter the quantity: (or kg for weighted items)");

            items.add(new OrderItem(item, quantity));

            char more = input.readNonEmptyCharOutOfPool("Add another item? (y/n)", new char[] {'y', 'n', 'Y', 'N'});
            if (more == 'n' || more == 'N') {
                addingItems = false;
            }
        }

        String promoInput = input.readOptionalString("Enter the promotion code (leave blank if none): ");
        String promoCode = promoInput.isBlank() ? null : promoInput.trim();

        String notes = input.readOptionalString("Enter optional delivery notes: ");

        Order order = orderService.createOrder(
                customer.getId(),
                restaurant.getId(),
                selectedAddress,
                items,
                promoCode,
                notes
        );

        System.out.println("\nOrder placed successfully!");
        printOrderSummary(order);
    }

    private void handlePayOrder(Customer customer) {
        List<Order> orders = orderService.listOrders().stream()
                .filter(order -> order.getCustomer().equals(customer))
                .toList();

        for (Order order : orders) {
            if (order == null) {
                break;
            }
            System.out.printf(" Order #%s | Date: %s | Total: %.2f EGP | Status: %s%n",
                    order.getId(),
                    order.getPlacedAt().toLocalDate(),
                    order.getTotal(),
                    order.getOrderStatus()
            );
        }

        String orderId = input.readNonEmptyString("\nEnter Order ID to pay: ");
        Order order = orderService.findOrder(orderId);

        if(!order.getCustomer().getId().equals(customer.getId())) {
            System.out.println("[ERROR] This order does not belong to you.");
            return;
        }

        orderService.payOrder(orderId);
        System.out.printf("Order %s successfully paid! Remaining wallet balance: %.2f EGP%n", orderId, customer.getWalletBalance());
    }

    private void handleTrackOrder() {
        String orderId = input.readNonEmptyString("\nEnter Order ID to track: ");
        Order order = orderService.findOrder(orderId);

        Duration elapsed = Duration.between(order.getPlacedAt(), LocalDateTime.now());
        long minutes = elapsed.toMinutes();

        System.out.printf("%nOrder ID: %s | Status: %s | Elapsed Time: %d minutes ago%n", orderId, order.getOrderStatus(), minutes);

        if (order.getAssignedRider() != null) {
            System.out.printf("Assigned Rider: %s (%s)%n", order.getAssignedRider().getName(), order.getAssignedRider().getVehicleType());
        }
    }

    private void handleCancelOrder(Customer customer) {
        String orderId = input.readNonEmptyString("\nEnter Order ID to cancel: ");
        Order order = orderService.findOrder(orderId);

        if (!order.getCustomer().getId().equals(customer.getId())) {
            System.out.println("[ERROR] this order does not belong to you.");
            return;
        }

        boolean isPaid = order.isPaid();

        orderService.cancelOrder(orderId);

        if (isPaid) {
            System.out.println("Order cancelled and refund processed.");
        } else {
            System.out.println("Order cancelled.");
        }
    }

    private void handleViewOrderHistory(Customer customer) {
        CustomerOrderHistory history = reportingService.getCustomerOrderHistory(customer);

        System.out.printf("%n=== Order History for %s (Total Spent: %.2f EGP) ===%n", customer.getName(), history.totalSpent());

        if (history.orders().isEmpty()) {
            System.out.println("No previous orders found.");
            return;
        }

        for (Order order : history.orders()) {
            if (order == null) {
                break;
            }
            System.out.printf(" Order #%s | Date: %s | Total: %.2f EGP | Status: %s%n",
                    order.getId(),
                    order.getPlacedAt().toLocalDate(),
                    order.getTotal(),
                    order.getOrderStatus()
                    );
        }
    }

    private void handleViewRecentSearches(Customer customer) {
        List<String> searches = customerService.getRecentSearches(customer.getId());

        if (searches.isEmpty()) {
            System.out.println("No recent searches recorded.");
            return;
        }

        System.out.printf("%n=== Last %d Searches === %n", searches.size());
        for (int i = 0; i < searches.size(); i++) {
            System.out.printf(" %d. %s%n", i + 1, searches.get(i));
        }
    }

    private void viewRestaurants() {
        List<Restaurant> restaurants = restaurantService.listRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found.");
            return;
        }
        System.out.println("\nRestaurants: ");
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurantIterator = restaurants.get(i);
            if (restaurantIterator == null) {
                break;
            }
            System.out.printf(" %d. %s [%s] (Rating: %.1f)%n",
                    i + 1,
                    restaurantIterator.getName(),
                    restaurantIterator.getDistrict(),
                    restaurantIterator.getRating()
            );
        }
    }

    private Restaurant selectRestaurantPrompt() {
        List<Restaurant> restaurants = restaurantService.listRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found.");
            return null;
        }
        System.out.println("\nSelect Restaurant: ");
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurantIterator = restaurants.get(i);
            if (restaurantIterator == null) {
                break;
            }
            System.out.printf(" %d. %s [%s] (Rating: %.1f)%n",
                    i + 1,
                    restaurantIterator.getName(),
                    restaurantIterator.getDistrict(),
                    restaurantIterator.getRating()
            );
        }

        int selectedIndex = input.readIntInRange("Enter your choice: ", 1, restaurants.size());
        return restaurants.get(selectedIndex - 1);
    }

    private void printRestaurantTable(List<Restaurant> list) {
        if (list.isEmpty()) {
            System.out.println("No restaurants found matching your criteria.");
            return;
        }

        System.out.printf("%n%-8s | %-20s | %-12s | %-6s | %s%n", "ID", "Name", "District", "Rating", "Cuisines");
        System.out.println("----------------------------------------------------------------------");
        for (Restaurant restaurant : list) {
            if (restaurant == null) {
                break;
            }
            String cuisines = restaurant.getCuisines().stream()
                    .map(Enum::name)
                    .collect(
                            Collectors.joining(", ")
                    );
            System.out.printf("%-8s | %-20s | %-12s | %-6.1f | %s%n",
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getDistrict(),
                    restaurant.getRating(),
                    cuisines
            );
        }
    }

    private void printOrderSummary(Order order) {
        System.out.println("========================================");
        System.out.printf("Order ID: %s | Status: %s%n",order.getId(), order.getOrderStatus());
        System.out.printf("Restaurant: %s | Delivery To: %s%n",
                order.getRestaurant().getName(), order.getDeliveryAddress().district()
        );
        System.out.println("Items:");
        for (OrderItem item : order.getOrderItems()) {
            if (item == null) {
                break;
            }
            System.out.printf("- %s x%.2f = %.2f EGP%n",
                    item.getMenuItem().getName(), item.getQuantity(), item.calculateLineTotal());
        }
        System.out.println("----------------------------------------");
        System.out.printf("Subtotal:     %.2f EGP%n", order.getSubtotal());
        System.out.printf("Delivery Fee: %.2f EGP%n", order.getDeliveryFee());
        System.out.printf("Service Fee:  %.2f EGP%n", order.getServiceFee());
        System.out.printf("Discount:    -%.2f EGP%n", order.getDiscount());
        System.out.println("----------------------------------------");
        System.out.printf("TOTAL:        %.2f EGP%n", order.getTotal());
        System.out.println("========================================");
    }

    private void handleViewRestaurantMenu() {
        Restaurant restaurant = selectRestaurantPrompt();
        if (restaurant == null) {
            return;
        }

        System.out.printf("%n=== Menu for %s (%s) ===%n", restaurant.getName(), restaurant.getDistrict());
        Map<String, MenuItem> menu = restaurant.getMenu();

        if (menu.isEmpty()) {
            System.out.println("This restaurant currently has no items on the menu");
            return;
        }

        printRestaurantMenu(menu);
    }

    private void handleTopUpWallet(Customer customer) {
        BigDecimal amount = input.readBigDecimal("Enter the amount to top up: ");
        customerService.rechargeWallet(customer.getId(), amount);
        System.out.println("Wallet balance updated successfully.");
    }
}
