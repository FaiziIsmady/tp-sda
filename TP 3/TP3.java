import java.io.*;
import java.util.*;

public class TP3 {
    public static InputReader in;
    public static PrintWriter out;

    // Current state variables
    public static int currentCity = 0; // Starting at city 1 (0-indexed)
    public static String currentPassword = "0000"; // Initial password

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
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

        // Precompute distances for all source cities used in R and F queries
        // We'll collect all currentCity states before each R or F query
        // However, since currentCity changes after M and J queries, precomputing isn't straightforward
        // Instead, compute distances on the fly or optimize accordingly
        // For simplicity, we'll compute distances on the fly when handling F queries

        // Precompute sizes for R queries using Union-Find for each energy from 1 to 100
        int MAX_ENERGY = 100;
        int[][] sizes = new int[V][MAX_ENERGY + 1]; // sizes[city][energy]

        // Sort all roads by length ascending
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
                    handleJQuery(start, allRoads, V);
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
            currentPassword = targetPassword; // Update password if successful
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
            for (int i = 0; i < size; i++) {
                int currentPass = queue.poll();

                // Try applying all available numbers
                for (String num : availableNumbers) {
                    int appliedPass = applyNumber(currentPass, num);

                    if (appliedPass == target) {
                        return steps + 1;
                    }

                    if (!visited[appliedPass]) {
                        visited[appliedPass] = true;
                        queue.add(appliedPass);
                    }
                }
            }
            steps++;
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

    public static void handleJQuery(int start, List<Edge> allRoads, int V) {
        int result = performJQuery(start, allRoads, V);
        out.println(result);
    }

    public static int performJQuery(int start, List<Edge> allRoads, int V) {
        // Initialize Union-Find for Kruskal's algorithm
        UnionFind uf = new UnionFind(V);
        int totalWeight = 0;

        // Collect all roads connected to the starting city
        List<Edge> connectedRoads = new ArrayList<>();
        for (Edge edge : allRoads) {
            if (edge.u == start || edge.v == start) {
                connectedRoads.add(edge);
            }
        }

        // Sort connectedRoads by length in ascending order
        connectedRoads.sort(Comparator.comparingInt(e -> e.l));

        // Include roads connected to the starting city in the MST if they don't form a cycle
        for (Edge edge : connectedRoads) {
            if (!uf.connected(edge.u, edge.v)) {
                uf.union(edge.u, edge.v);
                totalWeight += edge.l;
            }
        }

        // Collect remaining roads (excluding those connected to the starting city)
        List<Edge> remainingEdges = new ArrayList<>();
        for (Edge edge : allRoads) {
            if (edge.u != start && edge.v != start) {
                remainingEdges.add(edge);
            }
        }

        // Sort remaining edges by length in ascending order
        remainingEdges.sort(Comparator.comparingInt(e -> e.l));

        // Perform Kruskal's algorithm on the remaining edges
        for (Edge edge : remainingEdges) {
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

    public static class HeapNode {
        int node;
        int distance;

        public HeapNode(int node, int distance) {
            this.node = node;
            this.distance = distance;
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
