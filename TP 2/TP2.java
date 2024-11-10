import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP2 {
    private static InputReader in;
    private static PrintWriter out;
    private static AVLTree tree = new AVLTree();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        int N = in.nextInteger();
       

        for (int i = 0; i < N; i++) {
            //TODO:  process inputs
        }
        


        int A  = in.nextInteger();

          for (int i = 0; i < A; i++) {
            String query = in.next();
            switch (query) {
                case "G":
                    //TODO : Add character to the tree

                    break;
                case "R":
                    //TODO : Remove character from the tree
                    break;
                case "T":
                    //TODO : Print top 3 characters highest and lowest
                    break;
            }
        }
        out.close();
    }

    static void printTree(Node currPtr, String indent, boolean last) {
        if (currPtr != null) {
            out.print(indent);
            if (last) {
                out.print("R----");
                indent += "   ";
            } else {
                out.print("L----");
                indent += "|  ";
            }
            out.println(currPtr.character);
            printTree(currPtr.left, indent, false);
            printTree(currPtr.right, indent, true);
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

class Char {
    //TODO: Implement Char class
}

class Node {
     // TODO: modify attributes as needed
    Node left, right;
    Char character;

    Node(Char character) {
        this.character = character;
    }
}


class AVLTree{
    // TODO: modify attributes as needed
    Node root;


    List<Char> findTopThreeNodes(Node root) {
        // TODO: implement this method
        return null;
    }

    List<Char> findBottomThreeNodes(Node root) {
        // TODO: implement this method
        return null;
    }


    Node findParent(Node node, Char character) {
         // TODO: implement this method
         return null;
    }
    


    Node insert(Node node, Char character) {
         // TODO: implement this method
         return null;
    }

    Node delete(Node root, Char character) {
        // TODO: implement this method
        return null;
    }

   

    int getBalance(Node N) {
         // TODO: implement this method
         return 0;
    }

    Node findMax(Node node){
        // TODO: implement this method
        return null;
    }



    Node singleRightRotate(Node y) {
        // TODO: implement this method
        return null;
    }

    Node singleLeftRotate(Node x) {
         // TODO: implement this method
         return null;
    }
}