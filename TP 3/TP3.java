import java.io.*;
import java.util.*;

public class TP3 {
    private static InputReader in;
    private static PrintWriter out;

    // Current state variables
    private static int currentCity = 0; // Starting at city 1 (0-indexed)
    private static String currentPassword = "0000"; // Initial password

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        // Read number of cities (V) and number of roads (E)
        int V = in.nextInt();
        int E = in.nextInt();

        // Initialize the graph as an adjacency list
        List<int[]>[] graph = new ArrayList[V];
        for (int i = 0; i < V; i++) {
            graph[i] = new ArrayList<>();
        }

        // List to store all roads for Kruskal's algorithm (if needed)
        List<Edge> allRoads = new ArrayList<>();

        // Read each road and populate the graph and allRoads list
        for (int i = 0; i < E; i++) {
            int u = in.nextInt() - 1; // Convert to 0-indexed
            int v = in.nextInt() - 1; // Convert to 0-indexed
            int l = in.nextInt();
            graph[u].add(new int[]{v, l});
            graph[v].add(new int[]{u, l});
            allRoads.add(new Edge(u, v, l));
        }

        // Read available numbers for password
        int P = in.nextInt();
        List<String> availableNumbers = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            availableNumbers.add(in.next());
        }

        // Read number of queries (Q)
        int Q = in.nextInt();

        // Prepare a StringBuilder to batch output and improve I/O performance
        StringBuilder sb = new StringBuilder();

        // Process each query
        for (int i = 0; i < Q; i++) {
            String query = in.next(); // Read the query type

            if (query.equals("R")) {
                int energy = in.nextInt(); // R [ENERGI]
                int result = handleRQuery(graph, currentCity, energy, V);
                sb.append(result).append("\n");
            } else if (query.equals("F")) {
                int destination = in.nextInt() - 1; // F [TUJUAN]
                int result = handleFQuery(graph, currentCity, destination, V);
                sb.append(result).append("\n");
            } else if (query.equals("M")) {
                int id = in.nextInt() - 1; // M [ID]
                String password = in.next(); // [PASSWORD]
                int result = handleMQuery(id, password, availableNumbers, V);
                sb.append(result).append("\n");
                if (result != -1) {
                    currentPassword = password; // Update password if successful
                }
                currentCity = id; // Move to the new city regardless of password success
            } else if (query.equals("J")) {
                int start = in.nextInt() - 1; // J [ID]
                int result = handleJQuery(allRoads, start, V);
                sb.append(result).append("\n");
            }
        }

        // Output all results at once
        out.print(sb.toString());
        out.close();
    }

    private static int handleRQuery(List<int[]>[] graph, int start, int energy, int V) {
        boolean[] visited = new boolean[V];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        visited[start] = true;
        int reachableCities = 0;

        while (!queue.isEmpty()) {
            int currentCity = queue.poll();

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int distance = edge[1];
                if (!visited[neighbor] && distance <= energy) {
                    visited[neighbor] = true;
                    reachableCities++;
                    queue.add(neighbor); // Refill energy upon reaching the city
                }
            }
        }

        return reachableCities > 0 ? reachableCities : -1;
    }

    private static int handleFQuery(List<int[]>[] graph, int start, int destination, int V) {
        // Initialize distances with a large value
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        // Initialize the optimized MinHeap
        MinHeap heap = new MinHeap(V);
        heap.insert(start, 0);

        boolean[] visited = new boolean[V];

        while (!heap.isEmpty()) {
            HeapNode current = heap.extractMin();
            if (current == null) break;
            int currentCity = current.node;
            int currentDist = current.distance;

            if (visited[currentCity]) continue;
            visited[currentCity] = true;

            // Early exit if destination is reached
            if (currentCity == destination) {
                return currentDist;
            }

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int length = edge[1];
                if (!visited[neighbor] && currentDist + length < dist[neighbor]) {
                    dist[neighbor] = currentDist + length;
                    heap.insert(neighbor, dist[neighbor]);
                }
            }
        }

        return dist[destination] == Integer.MAX_VALUE ? -1 : dist[destination];
    }

    private static int handleMQuery(int id, String targetPassword, List<String> availableNumbers, int V) {
        // If the current password is already the target password, no steps are needed
        if (currentPassword.equals(targetPassword)) {
            return 0;
        }

        // BFS to find the minimum number of steps to reach the target password
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(currentPassword);
        visited.add(currentPassword);
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();

                // Try applying all available numbers
                for (String num : availableNumbers) {
                    String newPass = applyNumber(current, num);

                    if (newPass.equals(targetPassword)) {
                        return steps + 1;
                    }

                    if (!visited.contains(newPass)) {
                        visited.add(newPass);
                        queue.add(newPass);
                    }
                }
            }
            steps++;
            // To prevent infinite loops, set a reasonable limit (e.g., 10 steps)
            if (steps > 10) {
                break;
            }
        }

        return -1; // If target password is not reachable
    }

    private static String applyNumber(String current, String num) {
        StringBuilder newPass = new StringBuilder();
        for (int j = 0; j < 4; j++) {
            int sum = (current.charAt(j) - '0') + (num.charAt(j) - '0');
            newPass.append(sum % 10);
        }
        return newPass.toString();
    }

    private static int handleJQuery(List<Edge> allRoads, int start, int V) {
        // Initialize Union-Find for Kruskal's algorithm
        UnionFind uf = new UnionFind(V);
        int totalWeight = 0;

        // List to store roads connected to the starting city
        List<Edge> connectedRoads = new ArrayList<>();
        List<Edge> remainingRoads = new ArrayList<>();

        // Separate roads connected to the starting city and the rest
        for (Edge edge : allRoads) {
            if (edge.u == start || edge.v == start) {
                connectedRoads.add(edge);
            } else {
                remainingRoads.add(edge);
            }
        }

        // Include all roads connected to the starting city in the MST
        for (Edge edge : connectedRoads) {
            int u = edge.u;
            int v = edge.v;
            int l = edge.l;
            if (!uf.connected(u, v)) {
                uf.union(u, v);
                totalWeight += l;
            }
        }

        // Sort the remaining roads by their length in ascending order
        remainingRoads.sort(Comparator.comparingInt(e -> e.l));

        // Perform Kruskal's algorithm on the remaining roads
        for (Edge edge : remainingRoads) {
            int u = edge.u;
            int v = edge.v;
            int l = edge.l;
            if (!uf.connected(u, v)) {
                uf.union(u, v);
                totalWeight += l;
            }
        }

        // Check if all cities are connected
        int root = uf.find(0);
        for (int i = 1; i < V; i++) {
            if (uf.find(i) != root) {
                return -1; // Not all cities are connected
            }
        }

        return totalWeight;
    }

    static class Edge {
        int u;
        int v;
        int l;

        public Edge(int u, int v, int l) {
            this.u = u;
            this.v = v;
            this.l = l;
        }
    }

    static class UnionFind {
        int[] parent;
        int[] rank;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int fx = find(x);
            int fy = find(y);
            if (fx == fy) return;

            if (rank[fx] < rank[fy]) {
                parent[fx] = fy;
            } else {
                parent[fy] = fx;
                if (rank[fx] == rank[fy]) {
                    rank[fx]++;
                }
            }
        }

        public boolean connected(int x, int y) {
            return find(x) == find(y);
        }
    }

    static class MinHeap {
        private int[] heapNodes;    
        private int[] heapDistances; 
        private int size;
        private int capacity;

        public MinHeap(int capacity) {
            this.capacity = capacity;
            this.size = 0;
            this.heapNodes = new int[capacity];
            this.heapDistances = new int[capacity];
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public void insert(int node, int distance) {
            if (size == capacity) {
                // Double the capacity if the heap is full
                capacity *= 2;
                heapNodes = Arrays.copyOf(heapNodes, capacity);
                heapDistances = Arrays.copyOf(heapDistances, capacity);
            }
            heapNodes[size] = node;
            heapDistances[size] = distance;
            heapifyUp(size);
            size++;
        }

        public HeapNode extractMin() {
            if (isEmpty()) return null;
            HeapNode min = new HeapNode(heapNodes[0], heapDistances[0]);

            // Move the last element to the root
            heapNodes[0] = heapNodes[size - 1];
            heapDistances[0] = heapDistances[size - 1];
            size--;
            heapifyDown(0);

            return min;
        }

        private void heapifyUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                if (heapDistances[index] < heapDistances[parent]) {
                    swap(index, parent);
                    index = parent;
                } else {
                    break;
                }
            }
        }

        private void heapifyDown(int index) {
            while (true) {
                int smallest = index;
                int left = 2 * index + 1;
                int right = 2 * index + 2;

                if (left < size && heapDistances[left] < heapDistances[smallest]) {
                    smallest = left;
                }
                if (right < size && heapDistances[right] < heapDistances[smallest]) {
                    smallest = right;
                }
                if (smallest != index) {
                    swap(index, smallest);
                    index = smallest;
                } else {
                    break;
                }
            }
        }

        private void swap(int i, int j) {
            int tempNode = heapNodes[i];
            int tempDistance = heapDistances[i];
            heapNodes[i] = heapNodes[j];
            heapDistances[i] = heapDistances[j];
            heapNodes[j] = tempNode;
            heapDistances[j] = tempDistance;
        }
    }

    static class HeapNode {
        int node;
        int distance;

        public HeapNode(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

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
                    String line = reader.readLine();
                    if (line == null) {
                        return null; // End of input
                    }
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            String token = next();
            if (token == null) {
                throw new NoSuchElementException("No more tokens available");
            }
            return Integer.parseInt(token);
        }
    }
}
