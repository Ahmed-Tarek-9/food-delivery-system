package com.masrdelivery.app;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.District;
import com.masrdelivery.domain.common.id.*;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.domain.customer.LoyaltyTier;
import com.masrdelivery.domain.promotion.common.PromotionType;
import com.masrdelivery.domain.promotion.condition.DistrictCondition;
import com.masrdelivery.domain.promotion.condition.ExpiryDateCondition;
import com.masrdelivery.domain.promotion.condition.LoyaltyTierCondition;
import com.masrdelivery.domain.promotion.condition.MinimumOrderValueCondition;
import com.masrdelivery.domain.promotion.factory.PromotionConfig;
import com.masrdelivery.domain.restaurant.Cuisine;
import com.masrdelivery.domain.restaurant.MenuItem;
import com.masrdelivery.domain.restaurant.MenuItemCategory;
import com.masrdelivery.domain.restaurant.Restaurant;
import com.masrdelivery.domain.restaurant.common.MenuItemType;
import com.masrdelivery.domain.restaurant.factory.MenuItemFactory;
import com.masrdelivery.domain.rider.VehicleType;
import com.masrdelivery.repository.*;
import com.masrdelivery.service.*;
import com.masrdelivery.ui.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Application {

    private final Scanner scanner;
    private final InputReader inputReader;

    private CustomerConsoleView customerView;
    private RestaurantConsoleView restaurantView;
    private RiderConsoleView riderView;
    private AdminConsoleView adminView;

    public Application() {
        this.scanner = new Scanner(System.in);
        this.inputReader = new InputReader(scanner);
        initialize();
    }

    private void initialize() {
        // 1. Repositories
        CustomerRepository customerRepository = new CustomerRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        RiderRepository riderRepository = new RiderRepository();
        OrderRepository orderRepository = new OrderRepository();
        PromotionRepository promotionRepository = new PromotionRepository();
        DistrictDistanceRepository distanceRepository = new DistrictDistanceRepository();

        // 2. ID Generators
        CustomerIdGenerator customerIdGenerator = new CustomerIdGenerator();
        RestaurantIdGenerator restaurantIdGenerator = new RestaurantIdGenerator();
        RiderIdGenerator riderIdGenerator = new RiderIdGenerator();
        OrderIdGenerator orderIdGenerator = new OrderIdGenerator();
        MenuItemIdGenerator menuItemIdGenerator = new MenuItemIdGenerator();

        // 3. Factories
        MenuItemFactory menuItemFactory = new MenuItemFactory(menuItemIdGenerator);

        // 2. Services
        PricingService pricingService = new PricingService(distanceRepository);
        CustomerService customerService = new CustomerService(customerRepository, customerIdGenerator);
        RestaurantService restaurantService = new RestaurantService(restaurantRepository, restaurantIdGenerator, menuItemFactory);
        RiderService riderService = new RiderService(riderRepository, riderIdGenerator);
        PromotionService promotionService = new PromotionService(promotionRepository);
        OrderDispatchQueue orderDispatchQueue = new OrderDispatchQueue();

        OrderService orderService = new OrderService(
                orderRepository,
                customerService,
                restaurantService,
                promotionService,
                pricingService,
                riderService,
                orderIdGenerator,
                orderDispatchQueue
        );

        ReportingService reportingService = new ReportingService(
                customerRepository,
                orderRepository,
                promotionRepository,
                restaurantRepository,
                riderRepository
        );

        // 3. Seed Initial Demo Data
        seedPlatformData(customerService, restaurantService, riderService, promotionService);

        // 4. Views
        this.customerView = new CustomerConsoleView(
                customerService,
                restaurantService,
                orderService,
                reportingService,
                inputReader
        );

        this.restaurantView = new RestaurantConsoleView(
                restaurantService,
                orderService,
                inputReader
        );

        this.riderView = new RiderConsoleView(
                riderService,
                orderService,
                reportingService,
                inputReader
        );

        this.adminView = new AdminConsoleView(
                customerService,
                restaurantService,
                riderService,
                orderService,
                promotionService,
                reportingService,
                customerRepository,
                riderRepository,
                inputReader
        );
    }

    public void start() {
        System.out.println("=================================================");
        System.out.println("        WELCOME TO MASR DELIVERY PLATFORM        ");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU: SELECT PORTAL ---");
            System.out.println("1. Customer Portal");
            System.out.println("2. Restaurant Portal");
            System.out.println("3. Rider Portal");
            System.out.println("4. Admin & Analytics Portal");
            System.out.println("0. Exit Application");

            try {
                int roleChoice = inputReader.readIntInRange("Choose portal (0-4): ", 0, 4);
                switch (roleChoice) {
                    case 1 -> customerView.run();
                    case 2 -> restaurantView.run();
                    case 3 -> riderView.run();
                    case 4 -> adminView.run();
                    case 0 -> {
                        System.out.println("\nThank you for using Masr Delivery.");
                        running = false;
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            }
        }

        scanner.close();
    }

    private void seedPlatformData(
            CustomerService customerService,
            RestaurantService restaurantService,
            RiderService riderService,
            PromotionService promotionService
    ) {
        System.out.println("[SYSTEM] Seeding platform demo data...");

        // --- Customers ---
        Address ahmedAddress = new Address(District.MAADI, "Street 9, Building 45, Apt 3");
        Customer c1 = customerService.createCustomer(
                "Ahmed Hassan",
                "01012345678",
                ahmedAddress
        );

        Address sarahAddress = new Address(District.OCTOBER, "Al Tahrir ST, Apt 8");
        customerService.createCustomer(
                "Sarah El-Sayed",
                "01198765432",
                sarahAddress
        );

        Address mohamedAddress = new Address(District.DOKKI, "Mossadak St, Building 14");
        customerService.createCustomer(
                "Mohamed Tarek",
                "01233445566",
                mohamedAddress
        );

        // --- Restaurants & Menus ---
        Restaurant r1 = restaurantService.createRestaurant(
                "Koshary Abo Tarek",
                District.DOKKI,
                Set.of(Cuisine.EGYPTIAN, Cuisine.FAST_FOOD),
                new BigDecimal("4.8")
        );

        restaurantService.createMenuItem(
                r1.getId(),
                MenuItemType.STANDARD,
                "Mega Koshary Box",
                MenuItemCategory.PASTA,
                10,
                true,
                new BigDecimal("100.00"),
                new BigDecimal("55.00"),
                Collections.emptyList()
        );

        restaurantService.createMenuItem(
                r1.getId(),
                MenuItemType.STANDARD,
                "Rice Pudding with Nuts",
                MenuItemCategory.DESSERT,
                5,
                true,
                new BigDecimal("50.00"),
                new BigDecimal("25.00"),
                Collections.emptyList()
        );

        Restaurant r2 = restaurantService.createRestaurant(
                "Sobhy Kaber",
                District.OCTOBER,
                Set.of(Cuisine.EGYPTIAN, Cuisine.MIDDLE_EASTERN),
                new BigDecimal("4.7")
        );

        MenuItem kebabKg = restaurantService.createMenuItem(
                r2.getId(),
                MenuItemType.WEIGHTED,
                "Kebab & Kofta Mix (Per KG)",
                MenuItemCategory.GRILL,
                25,
                true,
                new BigDecimal("50.00"),
                new BigDecimal("580.00"),
                Collections.emptyList()
        );

        MenuItem molokhia = restaurantService.createMenuItem(
                r2.getId(),
                MenuItemType.STANDARD,
                "Tajin Molokhia with Garlic Tasha",
                MenuItemCategory.SIDE,
                15,
                true,
                new BigDecimal("80.00"),
                new BigDecimal("75.00"),
                Collections.emptyList()
        );

        restaurantService.createMenuItem(
                r2.getId(),
                MenuItemType.COMBO,
                "Royal Feast Combo",
                MenuItemCategory.COMBO,
                30,
                true,
                new BigDecimal("25.00"),
                new BigDecimal("620.00"),
                List.of(kebabKg, molokhia)
        );

        restaurantService.createRestaurant(
                "Crave Maadi",
                District.MAADI,
                Set.of(Cuisine.INTERNATIONAL, Cuisine.ITALIAN),
                new BigDecimal("4.6")
        );

        // --- Riders ---
        riderService.createRider("Mahmoud Ali", "01099887766", VehicleType.MOTORCYCLE, District.MAADI);
        riderService.createRider("Ibrahim Mostafa", "01155443322", VehicleType.MOTORCYCLE, District.OCTOBER);
        riderService.createRider("Khaled Samir", "01211223344", VehicleType.BICYCLE, District.FAISAL);

        // --- Promotions ---
        promotionService.createPromotion(
                "CAIRO20",
                "20% discount on orders above 150 EGP",
                new PromotionConfig(PromotionType.PERCENTAGE, new BigDecimal("20.00"), new BigDecimal("50.00")),
                List.of(
                        new MinimumOrderValueCondition(new BigDecimal("150.00")),
                        new ExpiryDateCondition(LocalDate.now().plusDays(30))
                )
        );

        promotionService.createPromotion(
                "FREESHIP",
                "Free delivery to Maadi district",
                new PromotionConfig(PromotionType.FREE_DELIVERY, null, null),
                List.of(
                        new DistrictCondition(District.MAADI),
                        new ExpiryDateCondition(LocalDate.now().plusDays(15))
                )
        );

        promotionService.createPromotion(
                "GOLDONLY",
                "Flat 50 EGP discount exclusively for Gold members",
                new PromotionConfig(PromotionType.FIXED_AMOUNT, new BigDecimal("50.00"), null),
                List.of(
                        new LoyaltyTierCondition(LoyaltyTier.GOLD),
                        new MinimumOrderValueCondition(new BigDecimal("200.00"))
                )
        );

        System.out.println("[SYSTEM] Platform demo data ready.\n");
    }
}