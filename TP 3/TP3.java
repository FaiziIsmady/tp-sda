import java.io.*;
import java.util.*;

public class TP3 {
    private static InputReader in;
    private static PrintWriter out;

    // Current state variables
    private static int currentCity = 0; // Starting at city 1 (0-indexed)
    private static String currentPassword = "0000"; // Initial password

    // Precomputed sizes for R queries
    private static int[][] sizes; // sizes[city][energy]
    private static final int MAX_ENERGY = 100;

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

        // List to store all roads for Kruskal's algorithm
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

        // Precompute sizes for R queries using Union-Find for each energy from 1 to 100
        precomputeSizes(allRoads, V);

        // Process each query using switch-case
        for (int i = 0; i < Q; i++) {
            String query = in.next(); // Read the query type

            switch (query) {
                case "R":
                    int energy = in.nextInt(); // R [ENERGI]
                    handleRQuery(energy, V);
                    break;

                case "F":
                    int destination = in.nextInt() - 1; // F [TUJUAN]
                    handleFQuery(destination, graph, V);
                    break;

                case "M":
                    int id = in.nextInt() - 1; // M [ID]
                    String password = in.next(); // [PASSWORD]
                    handleMQuery(id, password, availableNumbers, V);
                    break;

                case "J":
                    int start = in.nextInt() - 1; // J [ID]
                    handleJQuery(start, allRoads, V, graph);
                    break;

                default:
                    // Invalid query type
                    out.println("-1");
                    break;
            }
        }

        out.flush();
        out.close();
    }

    private static void precomputeSizes(List<Edge> allRoads, int V) {
        sizes = new int[V][MAX_ENERGY + 1]; // sizes[city][energy]
        // Sort all roads by length ascendingly
        allRoads.sort(Comparator.comparingInt(e -> e.l));

        // Initialize Union-Find
        UnionFind uf = new UnionFind(V);

        int roadIndex = 0;
        for (int energy = 1; energy <= MAX_ENERGY; energy++) {
            // Process all roads with L <= energy
            while (roadIndex < allRoads.size() && allRoads.get(roadIndex).l <= energy) {
                Edge edge = allRoads.get(roadIndex);
                uf.union(edge.u, edge.v);
                roadIndex++;
            }
            // For each city, record the size of its connected component at this energy
            for (int city = 0; city < V; city++) {
                sizes[city][energy] = uf.size(city);
            }
        }
    }

    private static void handleRQuery(int energy, int V) {
        if (energy < 1 || energy > MAX_ENERGY) {
            out.println("-1");
            return;
        }
        // Size of connected component at energy E for currentCity
        int componentSize = sizes[currentCity][energy];
        // Number of cities that can be visited excluding the starting city
        if (componentSize > 1) {
            out.println(componentSize - 1);
        } else {
            out.println("-1");
        }
    }

    private static void handleFQuery(int destination, List<int[]>[] graph, int V) {
        // Compute Dijkstra's from currentCity to destination
        int[] dist = dijkstra(currentCity, graph, V);
        if (dist[destination] == Integer.MAX_VALUE) {
            out.println("-1");
        } else {
            out.println(dist[destination]);
        }
    }

    private static void handleMQuery(int id, String targetPassword, List<String> availableNumbers, int V) {
        int result = performMQuery(targetPassword, availableNumbers);
        out.println(result);
        if (result != -1) {
            currentPassword = targetPassword; // Update password if successful
        }
        currentCity = id; // Move to the new city regardless of password success
    }

    private static int performMQuery(String targetPassword, List<String> availableNumbers) {
        // Convert passwords to integer representation
        int target = Integer.parseInt(targetPassword);
        int current = Integer.parseInt(currentPassword);

        // If the current password is already the target password, no steps are needed
        if (current == target) {
            return 0;
        }

        // BFS to find the minimum number of steps to reach the target password
        Queue<Integer> queue = new ArrayDeque<>();
        boolean[] visited = new boolean[10000];
        queue.add(current);
        visited[current] = true;
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            steps++;
            for (int i = 0; i < size; i++) {
                int currentPass = queue.poll();

                // Try applying all available numbers
                for (String num : availableNumbers) {
                    int appliedPass = applyNumber(currentPass, num);

                    if (appliedPass == target) {
                        return steps;
                    }

                    if (!visited[appliedPass]) {
                        visited[appliedPass] = true;
                        queue.add(appliedPass);
                    }
                }
            }
            // To prevent infinite loops, set a reasonable limit (e.g., 10 steps)
            if (steps > 10) {
                break;
            }
        }

        return -1; // If target password is not reachable
    }

    private static int applyNumber(int current, String num) {
        int newPass = 0;
        for (int j = 0; j < 4; j++) {
            int currentDigit = (current / (int) Math.pow(10, 3 - j)) % 10;
            int numDigit = num.charAt(j) - '0';
            int newDigit = (currentDigit + numDigit) % 10;
            newPass = newPass * 10 + newDigit;
        }
        return newPass;
    }

    private static void handleJQuery(int start, List<Edge> allRoads, int V, List<int[]>[] graph) {
        int result = performJQuery(start, allRoads, V);
        out.println(result);
    }

    private static int performJQuery(int start, List<Edge> allRoads, int V) {
        // Initialize Union-Find for Kruskal's algorithm
        UnionFind uf = new UnionFind(V);
        int totalWeight = 0;

        // Collect all roads connected to the starting city
        List<Edge> connectedRoads = new ArrayList<>();
        List<Edge> remainingRoads = new ArrayList<>();

        for (Edge edge : allRoads) {
            if (edge.u == start || edge.v == start) {
                connectedRoads.add(edge);
            } else {
                remainingRoads.add(edge);
            }
        }

        // Sort connectedRoads by length ascendingly
        connectedRoads.sort(Comparator.comparingInt(e -> e.l));

        // Include roads connected to the starting city in the MST if they don't form a cycle
        for (Edge edge : connectedRoads) {
            if (!uf.connected(edge.u, edge.v)) {
                uf.union(edge.u, edge.v);
                totalWeight += edge.l;
            }
        }

        // Sort remaining roads by length ascendingly
        remainingRoads.sort(Comparator.comparingInt(e -> e.l));

        // Perform Kruskal's algorithm on the remaining roads
        for (Edge edge : remainingRoads) {
            if (!uf.connected(edge.u, edge.v)) {
                uf.union(edge.u, edge.v);
                totalWeight += edge.l;
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

    private static int[] dijkstra(int source, List<int[]>[] graph, int V) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // Initialize the optimized MinHeap
        MinHeap heap = new MinHeap(V);
        heap.insert(source, 0);

        boolean[] visited = new boolean[V];

        while (!heap.isEmpty()) {
            HeapNode current = heap.extractMin();
            if (current == null) break;
            int currentCity = current.node;
            int currentDist = current.distance;

            if (visited[currentCity]) continue;
            visited[currentCity] = true;

            // Early exit if destination is handled in handleFQuery

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int length = edge[1];
                if (!visited[neighbor] && currentDist + length < dist[neighbor]) {
                    dist[neighbor] = currentDist + length;
                    heap.insert(neighbor, dist[neighbor]);
                }
            }
        }

        return dist;
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
        int[] size;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            this.size = new int[size];
            reset(size); // Initialize using the reset method
        }

        public void reset(int size) {
            for(int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
                this.size[i] = 1;
            }
        }

        // Find with path compression
        public int find(int x) {
            if(parent[x] != x){
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        // Union by rank
        public void union(int x, int y) {
            int fx = find(x);
            int fy = find(y);
            if(fx == fy) return;

            if(rank[fx] < rank[fy]){
                parent[fx] = fy;
                size[fy] += size[fx];
            }
            else{
                parent[fy] = fx;
                size[fx] += size[fy];
                if(rank[fx] == rank[fy]){
                    rank[fx]++;
                }
            }
        }

        // Check if two nodes are connected
        public boolean connected(int x, int y){
            return find(x) == find(y);
        }

        // Get the size of the connected component containing x
        public int size(int x){
            return size[find(x)];
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

    static class MinHeap {
        private HeapNode[] heap;
        private int size;
        private int capacity;

        public MinHeap(int capacity) {
            this.capacity = capacity;
            this.size = 0;
            heap = new HeapNode[capacity + 1]; // 1-based indexing
            heap[0] = new HeapNode(-1, Integer.MIN_VALUE); // Dummy node
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public void insert(int node, int distance) {
            if (size >= capacity) {
                // Resize the heap array if needed
                capacity *= 2;
                heap = Arrays.copyOf(heap, capacity + 1);
            }
            heap[++size] = new HeapNode(node, distance);
            siftUp(size);
        }

        public HeapNode extractMin() {
            if (size == 0) {
                return null;
            }
            HeapNode min = heap[1];
            heap[1] = heap[size--];
            siftDown(1);
            return min;
        }

        private void siftUp(int idx) {
            while (idx > 1 && heap[idx].distance < heap[idx / 2].distance) {
                swap(idx, idx / 2);
                idx = idx / 2;
            }
        }

        private void siftDown(int idx) {
            while (2 * idx <= size) {
                int j = 2 * idx;
                if (j < size && heap[j + 1].distance < heap[j].distance) {
                    j++;
                }
                if (heap[idx].distance <= heap[j].distance) {
                    break;
                }
                swap(idx, j);
                idx = j;
            }
        }

        private void swap(int i, int j) {
            HeapNode temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
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
