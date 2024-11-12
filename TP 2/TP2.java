import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

public class TP2 {
    private static InputReader in;
    private static PrintWriter out;
    private static AVLTree tree = new AVLTree();
    private static CircularLinkedList teams = new CircularLinkedList();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Baca jumlah tim
        int M = in.nextInteger();

        int[] teamSizes = new int[M];
        int[] points = new int[M * 2]; // Sesuaikan dengan jumlah peserta total

        // Inisialisasi ukuran tim
        for (int i = 0; i < M; i++) {
            teamSizes[i] = in.nextInteger();
        }

        // Inisialisasi poin awal peserta
        for (int i = 0; i < points.length; i++) {
            points[i] = in.nextInteger();
        }

        // Baca jumlah kueri
        int Q = in.nextInteger();

        // Loop untuk membaca setiap kueri
        for (int i = 0; i < Q; i++) {
            String query = in.next();
            
            switch (query) {
                case "A":
                    int jumlahPeserta = in.nextInteger();
                    // TODO: Add participants with default points
                    break;

                case "B":
                    String extremeBound = in.next();
                    // TODO: Calculate based on extremeBound (U or L)
                    break;

                case "M":
                    String direction = in.next();
                    // TODO: Move Sofita to the specified direction
                    break;

                case "T":
                    int senderId = in.nextInteger();
                    int receiverId = in.nextInteger();
                    int pointsToTransfer = in.nextInteger();
                    // TODO: Transfer points between participants
                    break;

                case "G":
                    String position = in.next();
                    // TODO: Create a new team
                    break;

                case "V":
                    int participant1 = in.nextInteger();
                    int participant2 = in.nextInteger();
                    int result = in.nextInteger();
                    // TODO: Process match results
                    break;

                case "E":
                    int minPoints = in.nextInteger();
                    // TODO: Eliminate teams with points below minPoints
                    break;

                case "U":
                    // TODO: Count unique points for participants in Sofita's team
                    break;

                case "R":
                    // TODO: Reorder teams
                    break;

                case "J":
                    String jDirection = in.next();
                    // TODO: Move Joki to the specified direction
                    break;

                default:
                    break;
            }
        }
        out.close();
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
    }
}

// Custom Linked List class for Circular Linked List of Teams
class CircularLinkedList {
    private TeamNode head;
    public TeamNode sofita;
    public TeamNode joki;

    class TeamNode {
        int teamId;
        TeamNode next;
        TeamNode prev;
        AVLTree participants; // Tree for storing participants in each team
        int totalPoints;

        TeamNode(int id) {
            this.teamId = id;
            this.participants = new AVLTree();
            this.totalPoints = 0; // Initialize total points
        }
    }

    // Add a new team to the linked list
    public void addTeam(int teamId) {
        TeamNode newNode = new TeamNode(teamId);
        if (head == null) {
            head = newNode;
            head.next = head;
            head.prev = head;
            sofita = head; // Initially Sofita starts watching the first team
        } else {
            TeamNode tail = head.prev;
            tail.next = newNode;
            newNode.prev = tail;
            newNode.next = head;
            head.prev = newNode;
        }
    }

    // Move Sofita to the left (counter-clockwise)
    public void moveSofitaLeft() {
        if (sofita != null) {
            sofita = sofita.prev;
        }
    }

    // Move Sofita to the right (clockwise)
    public void moveSofitaRight() {
        if (sofita != null) {
            sofita = sofita.next;
        }
    }

    // Move Joki to the left (counter-clockwise)
    public void moveJokiLeft() {
        if (joki != null) {
            joki = joki.prev;
        }
    }

    // Move Joki to the right (clockwise)
    public void moveJokiRight() {
        if (joki != null) {
            joki = joki.next;
        }
    }
}

// AVL Tree class for managing participant hierarchy within each team
class AVLTree {
    private Node root;

    class Node {
        Char character;
        Node left, right;
        int height;

        public Node(Char character) {
            this.character = character;
            this.height = 1;
        }
    }

    public void insert(Char character) {
        root = insertNode(root, character);
    }

    private Node insertNode(Node node, Char character) {
        if (node == null) {
            return new Node(character);
        }

        if (character.points < node.character.points) {
            node.left = insertNode(node.left, character);
        } else if (character.points > node.character.points) {
            node.right = insertNode(node.right, character);
        } else {
            // Handle tie-breaking by comparing matches, then ID
            if (character.matches < node.character.matches) {
                node.left = insertNode(node.left, character);
            } else if (character.matches > node.character.matches) {
                node.right = insertNode(node.right, character);
            } else {
                if (character.id < node.character.id) {
                    node.left = insertNode(node.left, character);
                } else {
                    node.right = insertNode(node.right, character);
                }
            }
        }

        // Update height and balance the tree
        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }

    private Node balance(Node node) {
        int balance = getBalance(node);

        if (balance > 1) {
            if (getBalance(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (balance < -1) {
            if (getBalance(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private int getBalance(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private Node rotateLeft(Node node) {
        Node newRoot = node.right;
        node.right = newRoot.left;
        newRoot.left = node;
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        newRoot.height = Math.max(height(newRoot.left), height(newRoot.right)) + 1;
        return newRoot;
    }

    private Node rotateRight(Node node) {
        Node newRoot = node.left;
        node.left = newRoot.right;
        newRoot.right = node;
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        newRoot.height = Math.max(height(newRoot.left), height(newRoot.right)) + 1;
        return newRoot;
    }

    // Find top 3 participants by points
    public List<Char> findTopThree() {
        List<Char> topThree = new ArrayList<>();
        findTopThree(root, topThree);
        return topThree.size() > 3 ? topThree.subList(0, 3) : topThree;
    }

    private void findTopThree(Node node, List<Char> result) {
        if (node == null || result.size() >= 3) return;
        findTopThree(node.right, result);
        if (result.size() < 3) result.add(node.character);
        findTopThree(node.left, result);
    }
}

class Char {
    int id;
    int points;
    int matches;

    public Char(int id, int points, int matches) {
        this.id = id;
        this.points = points;
        this.matches = matches;
    }
}
