import java.io.*;
import java.util.*;

public class TP3 {
    public static InputReader in;
    public static PrintWriter out;

    // Current state variables
    public static int currentCity = 0; // Starting at city 0 (0-indexed)
    public static String currentPassword = "0000"; // Initial password

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

        // Read number of queries (Q) and store all queries
        int Q = in.nextInt();
        List<Query> queries = new ArrayList<>(Q);

        for (int i = 0; i < Q; i++) {
            String queryType = in.next();
            if (queryType.equals("R")) {
                int energy = in.nextInt();
                queries.add(new Query(queryType, energy, -1, null));
                // 'R' queries do not affect currentCity
            } else if (queryType.equals("F")) {
                int destination = in.nextInt() - 1;
                queries.add(new Query(queryType, -1, destination, null));
                // 'F' queries do not affect currentCity
            } else if (queryType.equals("M")) {
                int id = in.nextInt() - 1;
                String password = in.next();
                queries.add(new Query(queryType, -1, id, password));
                // 'M' queries may change currentCity
            } else if (queryType.equals("J")) {
                int start = in.nextInt() - 1;
                queries.add(new Query(queryType, -1, start, null));
                // 'J' queries do not affect currentCity
            }
        }

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

        // Cache to store distance arrays for source cities
        Map<Integer, int[]> distanceCache = new HashMap<>();

        // Initially compute distances from the starting currentCity
        int[] dist = dijkstra(currentCity, graph, V);
        distanceCache.put(currentCity, dist);

        // Prepare a StringBuilder to batch output and improve I/O performance
        StringBuilder sb = new StringBuilder();

        // Process each query sequentially
        for (Query q : queries) {
            String queryType = q.type;
            if (queryType.equals("R")) {
                int energy = q.energy;
                int source = currentCity;
                if (energy < 1 || energy > MAX_ENERGY) {
                    sb.append("-1\n");
                } else {
                    int result = handleRQuery(source, energy, sizes);
                    sb.append(result).append("\n");
                }
            } else if (queryType.equals("F")) {
                int destination = q.destination;
                int source = currentCity;
                // Retrieve distances from the cache
                dist = distanceCache.get(source);
                int result = dist[destination] == Integer.MAX_VALUE ? -1 : dist[destination];
                sb.append(result).append("\n");
            } else if (queryType.equals("M")) {
                int id = q.destination; // In 'M' query, 'destination' holds 'id'
                String password = q.password;
                int result = handleMQuery(id, password, availableNumbers, V);
                sb.append(result).append("\n");
                if (result != -1) {
                    currentPassword = password; // Update the password if successful
                }
                currentCity = id; // Move to the new city regardless of password success

                // **Recompute distances from the new currentCity**
                dist = distanceCache.get(currentCity);
                if (dist == null) {
                    dist = dijkstra(currentCity, graph, V);
                    distanceCache.put(currentCity, dist);
                }
            } else if (queryType.equals("J")) {
                int start = q.destination; // In 'J' query, 'destination' holds 'start'
                int result = handleJQuery(allRoads, start, V);
                sb.append(result).append("\n");
            }
        }

        // Output all results at once
        out.print(sb.toString());
        out.close();
    }

    public static int handleRQuery(int source, int energy, int[][] sizes) {
        if (energy < 1 || energy > 100) return -1;
        // Size of connected component at energy E
        int componentSize = sizes[source][energy];
        // Number of cities that can be visited excluding the starting city
        return componentSize > 1 ? componentSize - 1 : -1;
    }

    public static int handleFQuery(int source, int destination, Map<Integer, int[]> distanceCache) {
        int[] dist = distanceCache.get(source);
        if (dist == null) {
            return -1; // Should not happen if distances are recomputed correctly
        }
        return dist[destination] == Integer.MAX_VALUE ? -1 : dist[destination];
    }

    public static int handleMQuery(int id, String targetPassword, List<String> availableNumbers, int V) {
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
            steps++;
            for (int i = 0; i < size; i++) {
                String current = queue.poll();

                // Try applying all available numbers
                for (String num : availableNumbers) {
                    String newPass = applyNumber(current, num);

                    if (newPass.equals(targetPassword)) {
                        currentPassword = newPass; // Update the password if successful
                        return steps;
                    }

                    if (!visited.contains(newPass)) {
                        visited.add(newPass);
                        queue.add(newPass);
                    }
                }
            }
        }

        return -1; // If target password is not reachable
    }

    public static String applyNumber(String current, String num) {
        StringBuilder newPass = new StringBuilder();
        for (int j = 0; j < 4; j++) {
            int sum = (current.charAt(j) - '0') + (num.charAt(j) - '0');
            newPass.append(sum % 10);
        }
        return newPass.toString();
    }

    public static int handleJQuery(List<Edge> allRoads, int start, int V) {
        // Initialize Union-Find for Kruskal's algorithm
        UnionFind uf = new UnionFind(V);
        int totalWeight = 0;

        // Include all roads connected to the starting city
        for (Edge edge : allRoads) {
            if (edge.u == start || edge.v == start) {
                if (!uf.connected(edge.u, edge.v)) {
                    totalWeight += edge.l;
                    uf.union(edge.u, edge.v); // Merge their endpoints
                }
            }
        }

        // Process remaining edges
        // Exclude edges connected to the starting city
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

    public static int[] dijkstra(int source, List<int[]>[] graph, int V) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // Use PriorityQueue for Dijkstra's algorithm
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        pq.add(new Node(source, 0));

        boolean[] visited = new boolean[V];

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int currentCity = current.city;
            if (visited[currentCity]) continue;
            visited[currentCity] = true;

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int length = edge[1];
                if (!visited[neighbor] && dist[currentCity] + length < dist[neighbor]) {
                    dist[neighbor] = dist[currentCity] + length;
                    pq.add(new Node(neighbor, dist[neighbor]));
                }
            }
        }

        return dist;
    }

    // Node class for PriorityQueue in Dijkstra's algorithm
    public static class Node {
        int city;
        int distance;

        public Node(int city, int distance) {
            this.city = city;
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

    public static class InputReader {
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

    public static class Query {
        String type;
        int energy; // For R queries
        int destination; // For F and J queries, and 'id' for M queries
        String password; // For M queries

        public Query(String type, int energy, int destination, String password) {
            this.type = type;
            this.energy = energy;
            this.destination = destination;
            this.password = password;
        }
    }
}
