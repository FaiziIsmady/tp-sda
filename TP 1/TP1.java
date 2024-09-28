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
    private static List<Customer> customerData = new ArrayList<>();
    private static Queue<Customer> customerQueue = new PriorityQueue<>(new CustomerComparator());
    private static Stack<Long> discountCoupons = new Stack<>();
    private static int customerIdCounter = 0;
    private static int timeGlobal = 0;

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
        // Decrement patience at the end of each process operation
        timeGlobal++;
        switch (operation.charAt(0)) {
            case 'A': // Add a new customer
                long budget = in.nextLong();
                int patience = in.nextInteger();
                addCustomer(budget, patience, true, timeGlobal);
                break;

            case 'S': // Find the closest fish price
                Long price = in.nextLong();
                findClosestFishPrice(price);
                break;

            case 'L': // Remove a customer by ID
                decrementPatience();
                long customerId = in.nextLong();
                removeCustomer(customerId);
                break;

            case 'D': // Add a discount coupon
                long discount = in.nextLong();
                addDiscount(discount, true);
                break;

            case 'B': // Serve the next customer
                decrementPatience();
                serveNextCustomer();
                break;

            case 'O': // Calculate happiness based on the query
                int queryType = in.nextInteger();
                int x = in.nextInteger();
                calculateHappiness(queryType, x);
                break;

            default:
                out.println("Invalid operation");
        }
    }

    // Method A = Adds a new customer with the given budget and patience
    private static void addCustomer(long budget, long patience, boolean inQueue, int entranceTime) {
        Customer newCustomer = new Customer(customerIdCounter++, budget, patience, patience, inQueue, timeGlobal); // timeGlobal keeps the time of when the customer enters
        customerData.add(newCustomer);
        customerQueue.add(newCustomer);
        out.println(newCustomer.id); // Output the ID of the new customer
    }

    // Decrements patience of all customers by 1 and removes those with patience 0
    private static void decrementPatience() {
        List<Customer> tempList = new ArrayList<>();

        // Iterate through each customer in the queue to decrement patience
        for (Customer customer : customerQueue) {
            customer.patience -= (timeGlobal - customer.entranceTime);
            customer.entranceTime = timeGlobal;

            // Remove customers whose patience has run out
            if (customer.patience <= 0) {
                tempList.add(customer);
                customer.inQueue = false; // Not in queue anymore
            }
        }

        // Remove the customers whose patience ran out
        customerQueue.removeAll(tempList);
    }

    // Method S = Implements finding the closest fish price using binary search.
    private static void findClosestFishPrice(Long price) {
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

    // Method L = Implements removing a customer from the queue by ID.
    private static void removeCustomer(long customerId) {
        long remainingBudget = -1;

        if (customerId > customerData.size() - 1 || customerId < 0) {
            out.println(remainingBudget);
        } else {
            Customer customer = customerData.get((int)customerId);
            if (customer.inQueue) {
                remainingBudget = customer.budget;
                customerQueue.remove(customer);
                customer.inQueue = false; // Set false (customer went out of the queue)
            }
            out.println(remainingBudget); // Output budget if found and removed
        }
    }

    // Method D = Adds a discount to the discount stack
    private static void addDiscount(Long discount, boolean printSize) {
        discountCoupons.push(discount);
        if (printSize) {
            out.println(discountCoupons.size()); // Only print the size when printSize is true
        }
    }

    // Method B = Implements serving the next customer in the queue.
    private static void serveNextCustomer() {
        // Check if there are no customers to serve
        if (customerQueue.isEmpty()) {
            out.println(-1);
            return;
        }
    
        // Poll the next customer in line (highest priority based on the comparator)
        Customer customer = customerQueue.poll();
        customer.inQueue = false;
    
        // Retrieve the original patience value from the map to reset it correctly
        long originalPatience = customer.originalPatience;
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
    
        // If no fish can be bought within the budget, the customer leaves the queue
        if (bestFishIndex == -1) {
            out.println(customer.id); // Output the customer's ID as they leave the queue
            return;
        }
    
        // Get the price of the fish the customer can buy
        long fishPrice = fishPrices.get(bestFishIndex);
    
        // Apply discount only if the fish price matches the customer's budget exactly
        if (remainingBudget == fishPrice) {
            if (!discountCoupons.isEmpty()) {
                long discount = discountCoupons.pop();
                fishPrice = Math.max(1, fishPrice - discount); // Discount applied cannot reduce below 1
            }
        } else {
            // Calculate the remaining budget after purchase add to discount stack
            addDiscount(remainingBudget-fishPrice, false);
        }

        remainingBudget -= fishPrice;
    
        // Output the remaining budget after purchase
        out.println(remainingBudget);
    
        // Readd customer regardless of remaining budget
        customer.budget = remainingBudget;
        customer.patience = originalPatience; // Reset to original patience value
        customerQueue.add(customer);
        customer.inQueue = true;
    }

    // TODO: Implement calculating happiness based on the query type and budget.
    private static void calculateHappiness(int queryType, int x) {
        int M = souvenirPrices.size();
        
        // DP arrays for three states
        int[][] dp0 = new int[M + 1][x + 1]; // State 0: no consecutive items
        int[][] dp1 = new int[M + 1][x + 1]; // State 1: one consecutive item
        int[][] dp2 = new int[M + 1][x + 1]; // State 2: two consecutive items
        
        // Path arrays to track indices
        List<Integer>[][] path0 = new ArrayList[M + 1][x + 1];
        List<Integer>[][] path1 = new ArrayList[M + 1][x + 1];
        List<Integer>[][] path2 = new ArrayList[M + 1][x + 1];
        
        // Initialize DP and path arrays
        for (int i = 0; i <= M; i++) {
            for (int j = 0; j <= x; j++) {
                dp0[i][j] = 0;
                dp1[i][j] = 0;
                dp2[i][j] = 0;
                path0[i][j] = new ArrayList<>();
                path1[i][j] = new ArrayList<>();
                path2[i][j] = new ArrayList<>();
            }
        }
        
        // DP processing with constraints
        for (int i = 1; i <= M; i++) {
            int price = souvenirPrices.get(i - 1);
            int value = souvenirValues.get(i - 1);
            
            for (int j = 0; j <= x; j++) {
                // Update state 0 (not taking the current item)
                dp0[i][j] = Math.max(dp0[i - 1][j], Math.max(dp1[i - 1][j], dp2[i - 1][j]));
                if (dp0[i][j] == dp0[i - 1][j]) {
                    path0[i][j] = new ArrayList<>(path0[i - 1][j]);
                } else if (dp0[i][j] == dp1[i - 1][j]) {
                    path0[i][j] = new ArrayList<>(path1[i - 1][j]);
                } else {
                    path0[i][j] = new ArrayList<>(path2[i - 1][j]);
                }
                
                // Update state 1 (taking the current item as the first in a sequence)
                if (j >= price) {
                    int newValue = dp0[i - 1][j - price] + value;
                    if (newValue > dp1[i][j]) {
                        dp1[i][j] = newValue;
                        path1[i][j] = new ArrayList<>(path0[i - 1][j - price]);
                        path1[i][j].add(i);
                    }
                }
                
                // Update state 2 (taking the current item as the second in a sequence)
                if (j >= price) {
                    int newValue = dp1[i - 1][j - price] + value;
                    if (newValue > dp2[i][j]) {
                        dp2[i][j] = newValue;
                        path2[i][j] = new ArrayList<>(path1[i - 1][j - price]);
                        path2[i][j].add(i);
                    }
                }
            }
        }
        
        // Find the maximum happiness for the given budget
        int maxHappiness = Math.max(dp0[M][x], Math.max(dp1[M][x], dp2[M][x]));
        List<Integer> resultPath = new ArrayList<>();
        
        // Determine which path corresponds to the max happiness
        if (maxHappiness == dp0[M][x]) {
            resultPath = path0[M][x];
        } else if (maxHappiness == dp1[M][x]) {
            resultPath = path1[M][x];
        } else {
            resultPath = path2[M][x];
        }
        
        // Output the result based on the query type
        if (queryType == 1) {
            out.println(maxHappiness); // For query type 1, only output the maximum happiness
        } else if (queryType == 2) {
            out.print(maxHappiness); // Output the maximum happiness and the path taken
            for (int index : resultPath) {
                out.print(" " + index);
            }
            out.println();
        }
    }

    // Customer class to hold customer data
    static class Customer {
        int id;
        long budget;
        long patience;
        long originalPatience;
        boolean inQueue;
        int entranceTime;

        public Customer(int id, long budget, long patience, long originalPatience, boolean inQueue, int entranceTime) {
            this.id = id;
            this.budget = budget;
            this.patience = patience;
            this.originalPatience = originalPatience;
            this.inQueue = inQueue;
            this.entranceTime = entranceTime;
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
