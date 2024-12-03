import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP3 {
    private static InputReader in;
    private static PrintWriter out;

    // Current state variables
    private static int currentCity = 0; // 0-indexed
    private static String currentPassword = "0000"; // Initial password

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        int V = in.nextInt();
        int E = in.nextInt();

        // Initialize graph
        List<int[]>[] graph = new ArrayList[V];
        for (int i = 0; i < V; i++) {
            graph[i] = new ArrayList<>();
        }

        // Read each road and populate the graph
        for (int i = 0; i < E; i++) {
            int u = in.nextInt() - 1; // 0-indexed
            int v = in.nextInt() - 1; // 0-indexed
            int l = in.nextInt();
            graph[u].add(new int[]{v, l});
            graph[v].add(new int[]{u, l});
        }

        int P = in.nextInt();
        List<String> availableNumbers = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            availableNumbers.add(in.next());
        }

        int Q = in.nextInt();

        for (int i = 0; i < Q; i++) {
            String query = in.next(); 

            if (query.equals("R")) {
                int energy = in.nextInt(); 
                int result = handleRQuery(graph, currentCity, energy, V);
                out.println(result);
            } else if (query.equals("F")) {
                int destination = in.nextInt() - 1; 
                int result = handleFQuery(graph, currentCity, destination, V);
                out.println(result);
            } else if (query.equals("M")) {
                int id = in.nextInt() - 1;
                String password = in.next(); 
                int result = handleMQuery(id, password, availableNumbers, V);
                out.println(result);
                if (result != -1) {
                    currentPassword = password; 
                }
                currentCity = id; 
            } else if (query.equals("J")) {
                int start = in.nextInt() - 1; 
                int result = handleJQuery(graph, start, V);
                out.println(result);
            }
        }

        out.close();
    }

    private static int handleRQuery(List<int[]>[] graph, int start, int energy, int V) {
        boolean[] visited = new boolean[V];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{start, energy});
        visited[start] = true;
        int reachableCities = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currentCity = current[0];
            int remainingEnergy = current[1];

            for (int[] edge : graph[currentCity]) {
                int neighbor = edge[0];
                int distance = edge[1];
                if (!visited[neighbor] && remainingEnergy >= distance) {
                    visited[neighbor] = true;
                    reachableCities++;
                    queue.add(new int[]{neighbor, energy}); 
                }
            }
        }

        return reachableCities == 0 ? -1 : reachableCities;
    }

    private static int handleFQuery(List<int[]>[] graph, int start, int destination, int V) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

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
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(currentPassword);
        visited.add(currentPassword);
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();

                for (String num : availableNumbers) {
                    StringBuilder newPassword = new StringBuilder();
                    for (int j = 0; j < 4; j++) {
                        int sum = (current.charAt(j) - '0') + (num.charAt(j) - '0');
                        newPassword.append(sum % 10);
                    }
                    String newPass = newPassword.toString();

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
            if (steps > 10) {
                break;
            }
        }

        return -1;
    }

    private static int handleJQuery(List<int[]>[] graph, int start, int V) {
        boolean[] connected = new boolean[V];
        int totalWeight = 0;

        for (int[] edge : graph[start]) {
            int neighbor = edge[0];
            int distance = edge[1];
            totalWeight += distance;
            connected[start] = true;
            connected[neighbor] = true;
        }

        MinHeap heap = new MinHeap(V);
        for (int i = 0; i < V; i++) {
            if (connected[i]) {
                for (int[] edge : graph[i]) {
                    int neighbor = edge[0];
                    int distance = edge[1];
                    if (!connected[neighbor]) {
                        heap.insert(neighbor, distance);
                    }
                }
            }
        }

        while (!heap.isEmpty()) {
            HeapNode current = heap.extractMin();
            if (current == null) break;
            int city = current.node;
            int distance = current.distance;

            if (connected[city]) continue;

            totalWeight += distance;
            connected[city] = true;

            for (int[] edge : graph[city]) {
                int neighbor = edge[0];
                int len = edge[1];
                if (!connected[neighbor]) {
                    heap.insert(neighbor, len);
                }
            }
        }

        for (int i = 0; i < V; i++) {
            if (!connected[i]) return -1;
        }

        return totalWeight;
    }

    static class MinHeap {
        private List<HeapNode> heap;

        public MinHeap(int capacity) {
            heap = new ArrayList<>(capacity);
        }

        public boolean isEmpty() {
            return heap.isEmpty();
        }

        public void insert(int node, int distance) {
            HeapNode newNode = new HeapNode(node, distance);
            heap.add(newNode);
            heapifyUp(heap.size() - 1);
        }

        public HeapNode extractMin() {
            if (heap.isEmpty()) return null;
            HeapNode min = heap.get(0);
            HeapNode last = heap.remove(heap.size() - 1);
            if (!heap.isEmpty()) {
                heap.set(0, last);
                heapifyDown(0);
            }
            return min;
        }

        private void heapifyUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                if (heap.get(index).distance < heap.get(parent).distance) {
                    swap(index, parent);
                    index = parent;
                } else {
                    break;
                }
            }
        }

        private void heapifyDown(int index) {
            int size = heap.size();
            while (true) {
                int smallest = index;
                int left = 2 * index + 1;
                int right = 2 * index + 2;

                if (left < size && heap.get(left).distance < heap.get(smallest).distance) {
                    smallest = left;
                }
                if (right < size && heap.get(right).distance < heap.get(smallest).distance) {
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
            HeapNode temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
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
                        return null;
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
