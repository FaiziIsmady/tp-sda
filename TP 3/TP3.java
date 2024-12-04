import java.io.*;
import java.util.*;

public class TP3 {
    public static InputReader in;
    public static PrintWriter out;

    // Current state variables
    public static int currentCity = 0; // Starting at city 1 (0-indexed)
    public static String currentPassword = "0000"; // Initial password

    // Declare a global Union-Find instance
    public static UnionFind globalUF;
    // Cache for J queries
    public static Map<Integer, Integer> jQueryCache = new HashMap<>();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read number of cities (V) and number of roads (E)
        int V = in.nextInt();
        int E = in.nextInt();

        // Initialize globalUF once after reading V
        globalUF = new UnionFind(V);

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
        int MAX_ENERGY = 100;
        int[][] sizes = new int[V][MAX_ENERGY + 1]; // sizes[city][energy]

        // Sort all roads by length ascendingly once
        List<Edge> sortedRoads = new ArrayList<>(allRoads);
        sortedRoads.sort(Comparator.comparingInt(e -> e.l));

        // Initialize a separate Union-Find instance for precomputing 'sizes'
        UnionFind ufPrecompute = new UnionFind(V);

        int roadIndex = 0;
        for (int energy = 1; energy <= MAX_ENERGY; energy++) {
            // Process all roads with L <= energy
            while (roadIndex < sortedRoads.size() && sortedRoads.get(roadIndex).l <= energy) {
                Edge edge = sortedRoads.get(roadIndex);
                ufPrecompute.union(edge.u, edge.v);
                roadIndex++;
            }
            // For each city, record the size of its connected component at this energy
            for (int city = 0; city < V; city++) {
                sizes[city][energy] = ufPrecompute.size(city);
            }
        }

        // Process each query directly using switch-case
        for (int i = 0; i < Q; i++) {
            String queryType = in.next();
            switch (queryType) {
                case "R":
                    int energy = in.nextInt();
                    handleRQuery(energy, sizes);
                    break;

                case "F":
                    int destination = in.nextInt() - 1;
                    handleFQuery(destination, graph, V);
                    break;

                case "M":
                    int id = in.nextInt() - 1;
                    String password = in.next();
                    handleMQuery(id, password, availableNumbers, V);
                    break;

                case "J":
                    int start = in.nextInt() - 1;
                    handleJQuery(start, sortedRoads, V);
                    break;

                default:
                    break;
            }
        }
        out.flush();
        out.close();
    }


    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream),32768);
            tokenizer = null;
        }

        public String next(){
            while(tokenizer == null || !tokenizer.hasMoreTokens()){
                try{
                    String line = reader.readLine();
                    if(line == null){
                        return null;
                    }
                    tokenizer = new StringTokenizer(line);
                }catch(IOException e){
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt(){
            String token = next();
            if(token == null){
                throw new NoSuchElementException("No more tokens available");
            }
            return Integer.parseInt(token);
        }
    }

    public static void handleRQuery(int energy, int[][] sizes) {
        if (energy < 1 || energy > 100) {
            out.println("-1");
            return;
        }
        // Size of connected component at energy E for currentCity
        int componentSize = sizes[currentCity][energy];
        // Number of cities that can be visited excluding the starting city
        int result = (componentSize > 1) ? (componentSize - 1) : -1;
        out.println(result);
    }

    public static void handleFQuery(int destination, List<int[]>[] graph, int V) {
        // Compute Dijkstra's from currentCity to destination
        int[] dist = dijkstra(currentCity, graph, V);
        int result = (dist[destination] == Integer.MAX_VALUE) ? -1 : dist[destination];
        out.println(result);
    }

    public static void handleMQuery(int id, String targetPassword, List<String> availableNumbers, int V) {
        int result = performMQuery(targetPassword, availableNumbers);
        out.println(result);
        if (result != -1) {
            currentPassword = String.format("%04d", Integer.parseInt(targetPassword)); // Update password if successful
        }
        currentCity = id; // Move to the new city regardless of password success
    }

    public static int performMQuery(String targetPassword, List<String> availableNumbers) {
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
        }

        return -1; // If target password is not reachable
    }

    public static int applyNumber(int current, String num) {
        int newPass = 0;
        for (int j = 0; j < 4; j++) {
            int currentDigit = (current / (int) Math.pow(10, 3 - j)) % 10;
            int numDigit = num.charAt(j) - '0';
            int newDigit = (currentDigit + numDigit) % 10;
            newPass = newPass * 10 + newDigit;
        }
        return newPass;
    }

    public static void handleJQuery(int start, List<Edge> sortedRoads, int V) {
        // Check if the result for this start city is already cached
        if (jQueryCache.containsKey(start)) {
            out.println(jQueryCache.get(start));
            return;
        }

        int result = performJQuery(start, sortedRoads, V, globalUF);
        // Cache the result
        jQueryCache.put(start, result);
        out.println(result);
    }

    public static int performJQuery(int start, List<Edge> sortedRoads, int V, UnionFind uf) {
        // Reset Union-Find for this J query
        uf.reset(V);
        int totalWeight = 0;
    
        // Lists to hold connected and remaining roads
        List<Edge> connectedRoads = new ArrayList<>();
        List<Edge> remainingRoads = new ArrayList<>();

        // Separate roads into connected and remaining
        for (Edge edge : sortedRoads) {
            if (edge.u == start || edge.v == start) {
                connectedRoads.add(edge);
            } else {
                remainingRoads.add(edge);
            }
        }

        // Sort connected roads ascendingly
        connectedRoads.sort(Comparator.comparingInt(e -> e.l));

        // Sort remaining roads ascendingly
        remainingRoads.sort(Comparator.comparingInt(e -> e.l));

        // Process connected roads first
        for (Edge edge : connectedRoads) {
            if (!uf.connected(edge.u, edge.v)) {
                uf.union(edge.u, edge.v);
                totalWeight += edge.l;
            }
        }

        // Then process remaining roads
        for (Edge edge : remainingRoads) {
            if (!uf.connected(edge.u, edge.v)) {
                uf.union(edge.u, edge.v);
                totalWeight += edge.l;
            }
        }

        // Check if all cities are connected to the starting city
        int root = uf.find(start);
        for (int i = 0; i < V; i++) { // Check all cities
            if (uf.find(i) != root) {
                return -1; // Not all cities are connected
            }
        }

        return totalWeight;
    }    

    public static int[] dijkstra(int source, List<int[]>[] graph, int V) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // Priority queue based on distance
        PriorityQueue<HeapNode> heap = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        heap.add(new HeapNode(source, 0));

        boolean[] visited = new boolean[V];

        while (!heap.isEmpty()) {
            HeapNode current = heap.poll();
            int currentCity = current.node;
            int currentDist = current.distance;

            if (visited[currentCity]) continue;
            visited[currentCity] = true;

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int length = edge[1];
                if (!visited[neighbor] && currentDist + length < dist[neighbor]) {
                    dist[neighbor] = currentDist + length;
                    heap.add(new HeapNode(neighbor, dist[neighbor]));
                }
            }
        }

        return dist;
    }

    public static class HeapNode implements Comparable<HeapNode> {
        int node;
        int distance;

        public HeapNode(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(HeapNode other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    public static class Edge {
        int u;
        int v;
        int l;

        public Edge(int u, int v, int l) {
            this.u = u;
            this.v = v;
            this.l = l;
        }
    }

    public static class UnionFind {
        int[] parent;
        int[] rank;
        int[] size;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            this.size = new int[size];
            reset(size); // Initialize using the reset method
        }

        /**
         * Resets the Union-Find structure to its initial state.
         *
         * @param size The number of elements in the Union-Find structure.
         */
        public void reset(int size) {
            for(int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
                this.size[i] = 1;
            }
        }

        public int find(int x) {
            if(parent[x] != x){
                parent[x] = find(parent[x]); // path compression
            }
            return parent[x];
        }

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

        public boolean connected(int x, int y){
            return find(x) == find(y);
        }

        public int size(int x){
            return size[find(x)];
        }
    }
}
