
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SmartBrew AI - Console-based coffee vending system prototype.
 *
 * Demonstrates customer authentication, beverage preferences, menu browsing,
 * inventory management, saved recipes, recommendations, simulated payments,
 * receipts, mood assistance, administrator functions, diagnostics and CSV export.
 *
 * Note: NFC authentication and payments are simulated. Data is stored in memory.
 */


public class SmartBrew {

    private static final Scanner sc = new Scanner(System.in);

    private static final Map<String, Integer> stock = new LinkedHashMap<>();
    private static final List<Drink> menu = new ArrayList<>();
    private static final List<Drink> savedRecipes = new ArrayList<>();
    private static final List<String> transactions = new ArrayList<>();
    private static final Map<String, String> preferences = new HashMap<>();

    private static String customer = null;
    private static int orderCounter = 1000;
    private static final int LOW_STOCK_LIMIT = 5;

    /**
     * Represents a beverage and its customization options.
     */
    static class Drink {
        String name;
        double price;
        Map<String, Integer> ingredients;
        String milk;
        String strength;
        String temperature;

        /**
         * Creates a beverage with its price, ingredients and preferences.
         *
         * @param name beverage name
         * @param price beverage price in SEK
         * @param ingredients ingredient names and quantities required
         * @param milk milk preference
         * @param strength coffee strength
         * @param temperature serving temperature
         * @return no value; initializes a Drink object
         */
        Drink(String name, double price,
              Map<String, Integer> ingredients,
              String milk, String strength, String temperature) {
            this.name = name;
            this.price = price;
            this.ingredients = new LinkedHashMap<>(ingredients);
            this.milk = milk;
            this.strength = strength;
            this.temperature = temperature;
        }

        /**
         * Creates an independent copy of this beverage.
         *
         * @param none
         * @return a new Drink object with copied ingredient quantities
         */
        Drink copy() {
            return new Drink(name, price, ingredients,
                    milk, strength, temperature);
        }
    }

    /**
     * Creates an ingredient map from alternating ingredient names and quantities.
     *
     * @param values alternating String ingredient names and Integer quantities
     * @return a map containing the specified ingredients and quantities
     */
    private static Map<String, Integer> ingredients(Object... values) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            result.put((String) values[i], (Integer) values[i + 1]);
        }

        return result;
    }

    /**
     * Initializes ingredient inventory, beverages and default preferences.
     *
     * @param none
     * @return void; populates the application's initial data
     */
    private static void initialize() {
        stock.put("Coffee beans", 100);
        stock.put("Milk", 100);
        stock.put("Oat milk", 50);
        stock.put("Water", 150);
        stock.put("Chocolate", 30);
        stock.put("Sugar", 40);

        menu.add(new Drink("Espresso", 20,
                ingredients("Coffee beans", 2, "Water", 1),
                "None", "Strong", "Hot"));

        menu.add(new Drink("Americano", 25,
                ingredients("Coffee beans", 2, "Water", 3),
                "None", "Medium", "Hot"));

        menu.add(new Drink("Cappuccino", 32,
                ingredients("Coffee beans", 2, "Milk", 3, "Water", 1),
                "Regular", "Medium", "Hot"));

        menu.add(new Drink("Latte", 35,
                ingredients("Coffee beans", 2, "Milk", 5, "Water", 1),
                "Regular", "Mild", "Hot"));

        menu.add(new Drink("Mocha", 38,
                ingredients("Coffee beans", 2, "Milk", 3,
                        "Chocolate", 2, "Water", 1),
                "Regular", "Medium", "Hot"));

        preferences.put("milk", "Regular");
        preferences.put("strength", "Medium");
        preferences.put("temperature", "Hot");
    }

    /**
     * Starts the application and displays the main menu.
     *
     * @param args command-line arguments; not used
     * @return void; runs until the user exits
     */
    public static void main(String[] args) {
        initialize();

        System.out.println("==================================");
        System.out.println("       SMARTBREW AI SYSTEM");
        System.out.println("==================================");
        System.out.println("Demo customer token: NFC-1001");
        System.out.println("Demo administrator PIN: 1234");

        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Customer sign in");
            System.out.println("2. Administrator sign in");
            System.out.println("0. Exit");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> customerLogin();
                case 2 -> adminLogin();
                case 0 -> {
                    System.out.println("Thank you for using SmartBrew AI.");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ============================================================
    // 1. CUSTOMER AUTHENTICATION
    // ============================================================

    /**
     * Authenticates a customer using a simulated NFC token.
     *
     * @param none; reads the token from the console
     * @return void; opens the customer menu if authentication succeeds
     */
    private static void customerLogin() {
        System.out.print("Enter demo NFC token: ");
        String token = sc.nextLine().trim();

        if (token.equals("NFC-1001")) {
            customer = "Demo Customer";
            System.out.println("Authentication successful. Welcome!");
            customerMenu();
        } else {
            System.out.println("Authentication failed: invalid NFC token.");
        }
    }

    /**
     * Displays customer operations and processes the selected option.
     *
     * @param none
     * @return void; continues until the customer signs out
     */
    private static void customerMenu() {
        while (customer != null) {
            System.out.println("\nCUSTOMER MENU - " + customer);
            System.out.println("1. Manage preferences");
            System.out.println("2. Browse beverages");
            System.out.println("3. Check ingredient inventory");
            System.out.println("4. Create and save a recipe");
            System.out.println("5. Get recommendations");
            System.out.println("6. Order a beverage");
            System.out.println("7. Mood assistant");
            System.out.println("8. View saved recipes");
            System.out.println("9. Sign out");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> managePreferences();
                case 2 -> browseMenu();
                case 3 -> checkInventory();
                case 4 -> saveRecipe();
                case 5 -> recommendDrink();
                case 6 -> placeOrder();
                case 7 -> moodAssistant();
                case 8 -> viewSavedRecipes();
                case 9 -> {
                    customer = null;
                    System.out.println("Signed out successfully.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ============================================================
    // 2. PREFERENCE MANAGEMENT
    // ============================================================

    /**
     * Allows the customer to configure milk, strength and temperature.
     *
     * @param none; choices are entered through the console
     * @return void; updates valid preferences in the preferences map
     */
    private static void managePreferences() {
        System.out.println("\nMILK: 1. Regular  2. Oat  3. None");
        int milkChoice = readInt("Choose milk: ");

        String milk;
        switch (milkChoice) {
            case 1 -> milk = "Regular";
            case 2 -> milk = "Oat";
            case 3 -> milk = "None";
            default -> {
                System.out.println("Invalid milk selection.");
                return;
            }
        }

        System.out.println("STRENGTH: 1. Mild  2. Medium  3. Strong");
        int strengthChoice = readInt("Choose strength: ");

        String strength;
        switch (strengthChoice) {
            case 1 -> strength = "Mild";
            case 2 -> strength = "Medium";
            case 3 -> strength = "Strong";
            default -> {
                System.out.println("Invalid strength.");
                return;
            }
        }

        System.out.println("TEMPERATURE: 1. Hot  2. Cold");
        int tempChoice = readInt("Choose temperature: ");

        String temperature;
        switch (tempChoice) {
            case 1 -> temperature = "Hot";
            case 2 -> temperature = "Cold";
            default -> {
                System.out.println("Invalid temperature.");
                return;
            }
        }

        // Update preferences only after all three inputs are valid.
        preferences.put("milk", milk);
        preferences.put("strength", strength);
        preferences.put("temperature", temperature);

        System.out.println("Preferences updated: " + preferences);
    }

    // ============================================================
    // 3. MENU BROWSING
    // ============================================================

    /**
     * Displays all available beverages and their prices.
     *
     * @param none
     * @return void; prints menu entries to the console
     */
    private static void browseMenu() {
        System.out.println("\nAVAILABLE BEVERAGES");

        for (int i = 0; i < menu.size(); i++) {
            Drink d = menu.get(i);
            System.out.printf("%d. %-15s %6.2f SEK%n",
                    i + 1, d.name, d.price);
        }
    }

    // ============================================================
    // 4. INVENTORY CHECKING
    // ============================================================

    /**
     * Displays inventory and checks the availability of a selected beverage.
     *
     * @param none; beverage selection is entered through the console
     * @return void; prints stock quantities and availability status
     */
    private static void checkInventory() {
        showStock();

        System.out.println("\nCheck a beverage's ingredient availability:");
        browseMenu();

        int choice = readInt("Select beverage (0 to cancel): ");

        if (choice == 0) {
            return;
        }

        if (choice < 1 || choice > menu.size()) {
            System.out.println("Invalid beverage.");
            return;
        }

        Drink drink = menu.get(choice - 1);

        if (hasIngredients(drink)) {
            System.out.println(drink.name + " is available.");
        } else {
            System.out.println(drink.name
                    + " is unavailable due to low stock.");
        }
    }

    /**
     * Checks whether sufficient quantities exist for every required ingredient.
     *
     * @param drink beverage whose ingredients must be checked
     * @return true if all required ingredients are available; otherwise false
     */
    private static boolean hasIngredients(Drink drink) {
        for (Map.Entry<String, Integer> entry
                : drink.ingredients.entrySet()) {

            if (stock.getOrDefault(entry.getKey(), 0)
                    < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Deducts a beverage's ingredient quantities from the inventory.
     *
     * @param drink beverage being prepared; its ingredients must be available
     * @return void; updates the stock map
     */
    private static void deductIngredients(Drink drink) {
        for (Map.Entry<String, Integer> entry
                : drink.ingredients.entrySet()) {

            stock.put(entry.getKey(),
                    stock.get(entry.getKey()) - entry.getValue());
        }
    }

    /**
     * Displays every ingredient and its current stock quantity.
     *
     * @param none
     * @return void; prints stock information
     */
    private static void showStock() {
        System.out.println("\nCURRENT STOCK");

        stock.forEach((name, quantity) ->
                System.out.println(name + ": " + quantity + " units"));
    }

    // ============================================================
    // 5. SAVED RECIPES
    // ============================================================

    /**
     * Creates a customized copy of a base beverage and saves the recipe.
     *
     * @param none; beverage selection and preferences come from console input
     * @return void; adds the customized beverage to savedRecipes
     */
    private static void saveRecipe() {
        browseMenu();

        int choice = readInt("Choose a base beverage (0 to cancel): ");

        if (choice == 0) {
            return;
        }

        if (choice < 1 || choice > menu.size()) {
            System.out.println("Invalid beverage.");
            return;
        }

        Drink recipe = menu.get(choice - 1).copy();
        recipe.name = recipe.name + " Custom";

        applyPreferences(recipe);

        savedRecipes.add(recipe);

        System.out.println("Recipe saved: " + recipe.name);
        System.out.println("Milk: " + recipe.milk
                + ", strength: " + recipe.strength
                + ", temperature: " + recipe.temperature);
    }

    /**
     * Applies current customer preferences to a beverage copy.
     *
     * @param drink beverage to customize
     * @return void; changes the drink's preferences and ingredient quantities
     */
    private static void applyPreferences(Drink drink) {
        drink.milk = preferences.get("milk");
        drink.strength = preferences.get("strength");
        drink.temperature = preferences.get("temperature");

        // Remove the original milk ingredient before choosing a milk type.
        drink.ingredients.remove("Milk");
        drink.ingredients.remove("Oat milk");

        if (drink.milk.equals("Regular")) {
            drink.ingredients.put("Milk", 3);
        } else if (drink.milk.equals("Oat")) {
            drink.ingredients.put("Oat milk", 3);
        }

        // Adjust coffee beans without replacing unrelated ingredients.
        int baseBeans = drink.ingredients.getOrDefault("Coffee beans", 2);

        if (drink.strength.equals("Mild")) {
            drink.ingredients.put("Coffee beans", Math.max(1, baseBeans - 1));
        } else if (drink.strength.equals("Strong")) {
            drink.ingredients.put("Coffee beans", baseBeans + 1);
        }
    }

    /**
     * Displays all saved customized recipes.
     *
     * @param none
     * @return void; prints recipe details or an empty-list message
     */
    private static void viewSavedRecipes() {
        if (savedRecipes.isEmpty()) {
            System.out.println("No saved recipes yet.");
            return;
        }

        System.out.println("\nSAVED RECIPES");

        for (int i = 0; i < savedRecipes.size(); i++) {
            Drink d = savedRecipes.get(i);

            System.out.printf("%d. %s | %.2f SEK | %s milk | %s | %s%n",
                    i + 1, d.name, d.price,
                    d.milk, d.strength, d.temperature);
        }
    }

    // ============================================================
    // 6. RECOMMENDATIONS
    // ============================================================

    /**
     * Recommends a beverage using the customer's current preferences.
     *
     * @param none; reads milk and strength from the preferences map
     * @return void; displays a rule-based recommendation
     */
    private static void recommendDrink() {
        String milk = preferences.get("milk");
        String strength = preferences.get("strength");

        String suggestion;

        if (strength.equals("Strong")) {
            suggestion = "Espresso";
        } else if (milk.equals("Oat")) {
            suggestion = "Americano";
        } else if (milk.equals("Regular")) {
            suggestion = "Latte";
        } else {
            suggestion = "Americano";
        }

        System.out.println("\nPERSONALIZED RECOMMENDATION");
        System.out.println("Based on your preferences, try: " + suggestion);
        System.out.println("Milk: " + milk);
        System.out.println("Strength: " + strength);
        System.out.println("Temperature: "
                + preferences.get("temperature"));
    }

    // ============================================================
    // 7. ORDER PROCESSING, PAYMENT AND RECEIPTS
    // ============================================================

    /**
     * Processes a beverage order, simulates payment and prints a receipt.
     *
     * @param none; selection and payment confirmation come from console input
     * @return void; records successful orders and deducts ingredients
     */
    private static void placeOrder() {
        System.out.println("\nORDER MENU");
        browseMenu();

        if (!savedRecipes.isEmpty()) {
            System.out.println("Saved recipes:");

            for (int i = 0; i < savedRecipes.size(); i++) {
                Drink d = savedRecipes.get(i);

                System.out.printf("%d. %s (%.2f SEK)%n",
                        menu.size() + i + 1, d.name, d.price);
            }
        }

        int choice = readInt("Select beverage (0 to cancel): ");

        if (choice == 0) {
            return;
        }

        Drink selected;

        if (choice >= 1 && choice <= menu.size()) {
            selected = menu.get(choice - 1).copy();
            applyPreferences(selected);
        } else if (choice > menu.size()
                && choice <= menu.size() + savedRecipes.size()) {
            selected = savedRecipes.get(choice - menu.size() - 1).copy();
        } else {
            System.out.println("Invalid selection.");
            return;
        }

        // Verify stock before asking for payment.
        if (!hasIngredients(selected)) {
            System.out.println("Order rejected: insufficient ingredients.");
            return;
        }

        System.out.printf("Total: %.2f SEK%n", selected.price);
        System.out.println("Simulated payment: 1. Confirm  2. Cancel");

        int payment = readInt("Choose: ");

        if (payment != 1) {
            System.out.println(
                    "Payment cancelled. No ingredients deducted.");
            return;
        }

        deductIngredients(selected);

        int orderId = ++orderCounter;

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String transaction = orderId + "," + timestamp + ","
                + customer + "," + selected.name + ","
                + String.format(Locale.US, "%.2f", selected.price)
                + ",SIMULATED_SUCCESS";

        transactions.add(transaction);

        printReceipt(orderId, timestamp, selected);
    }

    /**
     * Prints the receipt for a successfully simulated order.
     *
     * @param orderId unique order identifier
     * @param timestamp order date and time as a formatted string
     * @param drink beverage that was ordered
     * @return void; displays receipt information
     */
    private static void printReceipt(int orderId, String timestamp,
                                     Drink drink) {
        System.out.println("\n========== RECEIPT ==========");
        System.out.println("Order ID: " + orderId);
        System.out.println("Date: " + timestamp);
        System.out.println("Customer: " + customer);
        System.out.println("Beverage: " + drink.name);
        System.out.println("Milk: " + drink.milk);
        System.out.println("Strength: " + drink.strength);
        System.out.println("Temperature: " + drink.temperature);
        System.out.printf("Total: %.2f SEK%n", drink.price);
        System.out.println("Payment: SIMULATED SUCCESS");
        System.out.println("=============================");
    }

    // ============================================================
    // 8. MOOD ASSISTANT
    // ============================================================

    /**
     * Suggests a beverage based on the customer's entered mood.
     *
     * @param none; mood is entered as text through the console
     * @return void; prints a rule-based beverage suggestion
     */
    private static void moodAssistant() {
        System.out.println("\nHow are you feeling?");
        System.out.println("Examples: happy, tired, stressed, sad, focused");
        System.out.print("Enter mood: ");

        String mood = sc.nextLine().trim().toLowerCase(Locale.ROOT);

        switch (mood) {
            case "tired" ->
                    System.out.println("Try an Espresso for a strong coffee.");
            case "stressed" ->
                    System.out.println("Try a warm Latte and take a short break.");
            case "sad" ->
                    System.out.println("A Mocha may be a comforting choice.");
            case "happy" ->
                    System.out.println("Celebrate with a Cappuccino!");
            case "focused" ->
                    System.out.println("An Americano may suit your work session.");
            default ->
                    System.out.println("Try a Latte and choose what you enjoy.");
        }

        System.out.println(
                "This is a simple demo assistant, not a medical tool.");
    }

    // ============================================================
    // 9. ADMINISTRATOR AUTHENTICATION AND REFILL
    // ============================================================

    /**
     * Authenticates the administrator and opens the admin menu.
     *
     * @param none; administrator PIN is entered through the console
     * @return void; grants access only when the demo PIN is correct
     */
    private static void adminLogin() {
        System.out.print("Enter administrator PIN: ");
        String pin = sc.nextLine().trim();

        if (!pin.equals("1234")) {
            System.out.println("Access denied.");
            return;
        }

        System.out.println("Administrator authenticated.");

        while (true) {
            System.out.println("\nADMIN MENU");
            System.out.println("1. View inventory");
            System.out.println("2. Refill ingredient");
            System.out.println("3. Low-stock report");
            System.out.println("4. System diagnostics");
            System.out.println("5. Export transactions");
            System.out.println("0. Sign out");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> showStock();
                case 2 -> refillStock();
                case 3 -> lowStockReport();
                case 4 -> diagnostics();
                case 5 -> exportTransactions();
                case 0 -> {
                    System.out.println("Administrator signed out.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Adds a positive quantity to an existing ingredient's stock.
     *
     * @param none; ingredient name and refill quantity are entered by the admin
     * @return void; updates inventory when the input is valid
     */
    private static void refillStock() {
        showStock();

        System.out.print("Enter ingredient name exactly as listed: ");
        String ingredient = sc.nextLine().trim();

        if (!stock.containsKey(ingredient)) {
            System.out.println("Unknown ingredient.");
            return;
        }

        int quantity = readInt("Enter refill quantity: ");

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        stock.put(ingredient, stock.get(ingredient) + quantity);

        System.out.println(ingredient + " refilled successfully.");
        System.out.println("New quantity: " + stock.get(ingredient));
    }

    // ============================================================
    // 10. DIAGNOSTICS AND LOW-STOCK REPORT
    // ============================================================

    /**
     * Reports ingredients at or below the configured low-stock threshold.
     *
     * @param none
     * @return void; displays low-stock warnings or a normal-stock message
     */
    private static void lowStockReport() {
        System.out.println("\nLOW-STOCK REPORT");

        boolean low = false;

        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            if (entry.getValue() <= LOW_STOCK_LIMIT) {
                System.out.println("LOW STOCK: " + entry.getKey()
                        + " (" + entry.getValue() + " units)");
                low = true;
            }
        }

        if (!low) {
            System.out.println(
                    "All ingredients are above the low-stock limit.");
        }
    }

    /**
     * Displays basic application and subsystem status information.
     *
     * @param none
     * @return void; prints diagnostic information to the console
     */
    private static void diagnostics() {
        System.out.println("\nSYSTEM DIAGNOSTICS");
        System.out.println("Application: Running");
        System.out.println("Customer session: "
                + (customer == null ? "No customer signed in" : "Active"));
        System.out.println("Ingredient types: " + stock.size());
        System.out.println("Menu beverages: " + menu.size());
        System.out.println("Saved recipes: " + savedRecipes.size());
        System.out.println("Transactions in memory: " + transactions.size());
        System.out.println("Payment gateway: Simulated");
        System.out.println("NFC reader: Simulated token input");
        System.out.println("Database: Not configured");
        System.out.println("Low-stock limit: " + LOW_STOCK_LIMIT + " units");
    }

    /**
     * Exports successful transaction records to a CSV file.
     *
     * @param none
     * @return void; creates or overwrites smartbrew_transactions.csv
     *         and reports any file-writing error
     */
    private static void exportTransactions() {
        String fileName = "smartbrew_transactions.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(
                    "OrderID,Timestamp,Customer,Beverage,PriceSEK,PaymentStatus\n");

            for (String transaction : transactions) {
                writer.write(transaction + "\n");
            }

            System.out.println("Transactions exported to: " + fileName);
            System.out.println("Exported rows: " + transactions.size());

        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // ============================================================
    // INPUT VALIDATION HELPER
    // ============================================================

    /**
     * Reads an integer from the console and retries after invalid input.
     *
     * @param prompt message displayed before reading input
     * @return the integer entered by the user
     */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}
