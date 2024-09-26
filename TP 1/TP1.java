import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP1 {
    private static InputReader in;
    private static PrintWriter out;

    // Global variables for managing customers, fish prices, souvenirs, etc.
    private static List<Long> fishPrices = new ArrayList<>();
    private static List<Integer> souvenirPrices = new ArrayList<>();
    private static List<Integer> souvenirValues = new ArrayList<>();
    private static Queue<Customer> customerQueue = new PriorityQueue<>(new CustomerComparator());
    private static Stack<Integer> discountCoupons = new Stack<>();
    private static Map<Integer, Long> originalPatienceMap = new HashMap<>();
    private static int customerIdCounter = 0;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read the initial inputs
        int N = in.nextInteger(); // Number of fish prices
        int M = in.nextInteger(); // Number of souvenirs
        int Q = in.nextInteger(); // Number of operations

        // Read fish prices
        for (int i = 0; i < N; i++) {
            fishPrices.add(in.nextLong());
        }

        // Read souvenir prices
        for (int i = 0; i < M; i++) {
            souvenirPrices.add(in.nextInteger());
        }

        // Read souvenir values
        for (int i = 0; i < M; i++) {
            souvenirValues.add(in.nextInteger());
        }

        // Process each operation
        for (int i = 0; i < Q; i++) {
            String operation = in.next();
            processOperation(operation);
        }

        out.close();
    }

    // Processes each operation based on the input
    private static void processOperation(String operation) {

        decrementPatience();

        switch (operation.charAt(0)) {
            case 'A': // Aman
                // Read budget and patience, add customer
                long budget = in.nextLong();
                long patience = in.nextLong();
                addCustomer(budget, patience);
                break;

            case 'S':
                // Read desired fish price, find the closest price
                Long price = in.nextLong();
                findClosestFishPrice(price);
                break;

            case 'L':
                // Read customer ID, remove from queue
                int customerId = in.nextInteger();
                removeCustomer(customerId);
                break;

            case 'D':
                // Read discount value, add to discount stack
                int discount = in.nextInteger();
                addDiscount(discount);
                break;

            case 'B':
                // Serve the next customer in line
                serveNextCustomer();
                break;

            case 'O':
                // Read query type and budget, calculate happiness
                int queryType = in.nextInteger();
                int x = in.nextInteger();
                calculateHappiness(queryType, x);
                break;

            default:
                out.println("Invalid operation");
        }
    }

    // Adds a new customer with the given budget and patience
    private static void addCustomer(long budget, long patience) {
        // Customer ID is auto-incremented based on the count of incoming customers
        Customer newCustomer = new Customer(customerIdCounter++, budget, patience);
        customerQueue.add(newCustomer);
        originalPatienceMap.put(newCustomer.id, patience); // Save the original patience value
        out.println(newCustomer.id); // Output the ID of the new customer
    }

    // TODO: Implement finding the closest fish price using binary search.
    // Implements finding the closest fish price using a simple manual binary search.
    private static void findClosestFishPrice(Long price) {
        if (fishPrices.isEmpty()) {
            System.out.println("No fish prices available");
            return;
        }

        int left = 0;
        int right = fishPrices.size() - 1;

        // Initialize variable to keep track of the closest price
        long closestPrice = fishPrices.get(0);

        // Perform binary search to find the closest price
        while (left <= right) {
            int mid = left + (right - left) / 2;

            // Update the closest price if the current mid is closer
            if (Math.abs(fishPrices.get(mid) - price) < Math.abs(closestPrice - price)) {
                closestPrice = fishPrices.get(mid);
            } else if (Math.abs(fishPrices.get(mid) - price) == Math.abs(closestPrice - price)) {
                // In case of a tie, prefer the smaller price
                closestPrice = Math.min(closestPrice, fishPrices.get(mid));
            }

            // Adjust search range based on comparison
            if (fishPrices.get(mid) < price) {
                left = mid + 1; // Search in the right half
            } else {
                right = mid - 1; // Search in the left half
            }
        }

        // Output the minimum difference between the queried price and the closest price found
        out.println(Math.abs(closestPrice - price));
    }

    // TODO: Implement removing a customer from the queue by ID.
    // Implements removing a customer from the queue by ID.
    private static void removeCustomer(int customerId) {
        boolean found = false;
        long remainingBudget = -1;

        // Use a temporary list to hold the customers
        List<Customer> tempList = new ArrayList<>();

        // Transfer customers from the queue to the temporary list, checking each one
        while (!customerQueue.isEmpty()) {
            Customer current = customerQueue.poll();

            // Check if this is the customer to be removed
            if (current.id == customerId) {
                found = true;
                remainingBudget = current.budget; // Store the remaining budget of the removed customer
            } else {
                tempList.add(current); // Add back to the temporary list if not the target
            }
        }

        // Re-add all customers back to the queue
        customerQueue.addAll(tempList);

        // Output the remaining budget if the customer was found, otherwise output -1
        if (found) {
            out.println(remainingBudget);
        } else {
            out.println(-1);
        }
    }

    // Adds a discount to the discount stack
    private static void addDiscount(int discount) {
        discountCoupons.push(discount);
        out.println(discountCoupons.size()); // Output the size of the discount stack
    }

    // TODO: Implement serving the next customer in the queue.
    // Implements serving the next customer in the queue.
    // Implements serving the next customer in the queue.
    // Implements serving the next customer in the queue.
    private static void serveNextCustomer() {
        // Check if there are no customers to serve
        if (customerQueue.isEmpty()) {
            out.println(-1);
            return;
        }

        // Poll the next customer in line (highest priority based on the comparator)
        Customer customer = customerQueue.poll();

        // Retrieve the original patience value from the map to reset it correctly
        long originalPatience = originalPatienceMap.get(customer.id);
        long remainingBudget = customer.budget;

        // Find the most expensive fish the customer can afford using binary search
        int left = 0;
        int right = fishPrices.size() - 1;
        int bestFishIndex = -1;

        // Binary search to find the most expensive fish within the customer's budget
        while (left <= right) {
            int mid = left + (right - left) / 2;

            // Check if the current mid fish price is affordable
            if (fishPrices.get(mid) <= remainingBudget) {
                bestFishIndex = mid; // Update the best affordable fish
                left = mid + 1; // Try to find a more expensive option
            } else {
                right = mid - 1; // Search in the left half if the fish is too expensive
            }
        }

        // If no fish can be bought within the budget
        if (bestFishIndex == -1) {
            out.println(customer.id); // Output the customer's ID as they leave the queue
            printCustomerQueue(); // Debug: Print the customer queue state
            return;
        }

        // Get the price of the fish the customer can buy
        long fishPrice = fishPrices.get(bestFishIndex);
        System.out.println("Best fish price = " + fishPrice);

        // Apply discount if available and if it helps to afford a fish or reduces the price to 1
        if (!discountCoupons.isEmpty()) {
            int discount = discountCoupons.peek();
            if (fishPrice - discount <= remainingBudget) {
                // Apply the discount only if it helps the customer buy the fish affordably
                fishPrice = Math.max(1, fishPrice - discount); // Discount applied cannot reduce below 1
                discountCoupons.pop(); // Use the discount coupon
            }
        }

        // Calculate the remaining budget after purchase
        remainingBudget -= fishPrice;

        // Output the remaining budget after purchase
        System.out.println("Remaining budget for customer ID " + customer.id + " = " + remainingBudget);
        out.println(remainingBudget);

        // Reset patience to the original value and re-add the customer if they still have money left
        if (remainingBudget > 0) {
            customer.budget = remainingBudget;
            customer.patience = originalPatience; // Reset to original patience value
            customerQueue.add(customer);
        } else {
            // If the customer has no budget left, output that they have finished
            System.out.println("Customer ID " + customer.id + " has no budget left and leaves.");
        }

        printCustomerQueue(); // Debug: Print the customer queue state after serving
    }

    // TODO: Implement calculating happiness based on the query type and budget.
    private static void calculateHappiness(int queryType, int x) {
        // Implement logic to calculate the maximum happiness with constraints
        // Use dynamic programming if necessary, depending on queryType 1 or 2
    }

    // Customer class to hold customer data
    static class Customer {
        int id;
        long budget;
        long patience;

        public Customer(int id, long budget, long patience) {
            this.id = id;
            this.budget = budget;
            this.patience = patience;
        }
    }

    // Comparator for managing customer priority in the queue
    static class CustomerComparator implements Comparator<Customer> {
        @Override
        public int compare(Customer c1, Customer c2) {
            // Compare by highest budget (descending order)
            int budgetComparison = Long.compare(c2.budget, c1.budget);
            if (budgetComparison != 0) return budgetComparison;

            // Compare by lowest patience (ascending order)
            int patienceComparison = Long.compare(c1.patience, c2.patience);
            if (patienceComparison != 0) return patienceComparison;

            // Compare by lowest ID (ascending order)
            return Integer.compare(c1.id, c2.id);
        }
    }

    // Decrements patience of all customers by 1 and removes those with patience 0
    private static void decrementPatience() {
        List<Customer> tempList = new ArrayList<>();

        // Iterate through each customer in the queue to decrement patience
        while (!customerQueue.isEmpty()) {
            Customer customer = customerQueue.poll();
            customer.patience--;

            // Only re-add customers who still have patience left
            if (customer.patience > 0) {
                tempList.add(customer);
            } else {
                // Customer loses patience and leaves the queue, no re-addition
                System.out.println("Customer ID " + customer.id + " has lost patience and leaves.");
            }
        }

        // Re-add remaining customers back to the queue
        customerQueue.addAll(tempList);
    }

    // Debug function to print the current state of the customer queue
    private static void printCustomerQueue() {
        System.out.println("Current customer queue state:");
        for (Customer customer : customerQueue) {
            System.out.println("ID: " + customer.id + ", Budget: " + customer.budget + ", Patience: " + customer.patience);
        }
    }

    // Faster input reading class
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInteger() {
            return Integer.parseInt(next());
        }

        public long nextLong() {
            return Long.parseLong(next());
        }
    }
}
