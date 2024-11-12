import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

class Visitor {
    int id;
    int budget;
    int patience;

    public Visitor(int id, int budget, int patience) {
        this.id = id;
        this.budget = budget;
        this.patience = patience;
    }
}

class ComparingVisitor implements java.util.Comparator<Visitor> {

    public int compare(Visitor x, Visitor y) {
        // Prioritas 1: tingkat budget lebih tinggi
        if (x.budget != y.budget) {
            return Integer.compare(y.budget, x.budget);
        }
        // Prioritas 2: tingkat kesabaran lebih tinggi
        if (x.patience != y.patience) {
            return Integer.compare(x.patience, y.patience); // Urutan menurun
        }
        // Prioritas 3: ID lebih kecil
        return Integer.compare(x.id, y.id); // Urutan menaik
    }
}
public class TP111{
    private static InputReader in;
    private static PrintWriter out;
    private static PriorityQueue<Visitor> visitorQueue = new PriorityQueue<>(new ComparingVisitor());
    private static Stack<Integer> discountCoupon = new Stack<>();
    private static ArrayList<Integer> patienceList = new ArrayList<>();
    private static int maxPossible;

    private static StringBuilder oIndex = new StringBuilder();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);
        // basic info
        int id = 0;

        // Read inputs
        int fish = in.nextInt();
        int souvenir = in.nextInt();
        int vistor = in.nextInt();
        int[] fishPrice = new int[fish];
        int[] souvenirPrice = new int[souvenir];
        int[] souvenirHappines = new int[souvenir];
        int[] answers = new int[vistor];


        for (int i = 0; i < fish ; i ++){
            fishPrice[i] = in.nextInt();
        }
        for (int j = 0; j < souvenir ; j ++){
            souvenirPrice[j] = in.nextInt();
            maxPossible += souvenirPrice[j];
        }
        for (int k = 0; k < souvenir ; k ++){
            souvenirHappines[k] = in.nextInt();
        }
        for (int l = 0 ; l < vistor ; l ++){
            updateQueue();
            boolean isO = false;
            switch (in.next().charAt(0)){
                case 'A':
                    handleA(answers, l, id);
                    id++;
                    break;
                case 'B' :
                    handleB(answers, l, fishPrice);
                    break;
                case 'D' :
                    handleD(answers, l, in.nextInt());
                    break;
                case 'L':
                    int tempID = in.nextInt();
                    handleL(answers, l, tempID);
                    break;
                case 'O' :
                    handleO(answers,l, in.nextInt(), in.nextInt(), souvenirPrice,souvenirHappines);
                    isO = true;
                    break;
                case 'S':
                    handleS(answers, l, in.nextInt() , fishPrice);
                    break;
            }
            out.print(answers[l]);
            if (!isO){
                out.print("\n");
            } else {
                out.println(oIndex);
            }
        }
        out.close();
    }

    public static void handleA(int[] answers, int index , int id){
        int budget = in.nextInt();
        int patience = in.nextInt();
        patienceList.add(patience);
        visitorQueue.add(new Visitor(id, budget,patience));
        answers[index] = id;
    }

    public static int maxAffordablePrice(int budget, int[] productPrice){
        int lowIndex = 0;
        int highIndex = productPrice.length - 1;
        int upperLimitPrice = 0;
        while (lowIndex <= highIndex){
            int mid = ( lowIndex + highIndex ) / 2;
            if (productPrice[mid] == budget){
                return budget;
            }
            if (productPrice[mid] < budget) {
                upperLimitPrice = productPrice[mid];
                lowIndex = mid + 1;
            } else {
                highIndex = mid - 1;
            }
        }
        return upperLimitPrice;
    }
    public static void handleB(int[] answers, int index, int[] fishPrice){
        if (visitorQueue.isEmpty()){
            answers[index] = -1;
            return;
        }

        Visitor temp = visitorQueue.poll();
        int affordableFish = maxAffordablePrice(temp.budget,fishPrice);

        if (affordableFish == 0){
            answers[index] =temp.id;
        } else {
            temp.patience = patienceList.get(temp.id);

            if(affordableFish == temp.budget) {
                if (!discountCoupon.isEmpty()) {
                    temp.budget -= Math.max(affordableFish - discountCoupon.pop(), 1);
                } else {
                    temp.budget -= affordableFish;
                }
            } else {
                temp.budget -= affordableFish;
                discountCoupon.push(temp.budget);
            }

            answers[index] = temp.budget;
            visitorQueue.add(temp);
        }
    }

    public static void handleD(int[] answers, int index, int value){
        discountCoupon.push(value);
        answers[index] = discountCoupon.size();
    }

    public static void handleL(int[] answers, int index, int tempID){
        for (Visitor temp : visitorQueue){
            if (temp.id == tempID ) {
                answers[index] = temp.budget;
                visitorQueue.remove(temp);
                return;
            }
        }
        answers[index] = -1;
    }
    public static void handleO(int[] answers, int index, int queryType, int searchPrice,int[] souvenirPrice, int[]souvenirHappiness) {
        // happinessCondition untuk menyimpan total kebahagiaan maks (dimensi 1 = jumlah  barang, dimensi 2 = harga, dimensi 3 = status index)
        int[][][] happinessCondition = new int[souvenirPrice.length + 1][maxPossible + 1][3];

        for (int i = 1; i <= souvenirPrice.length; i++) {
            for (int j = 0; j <= maxPossible; j++) {
                if (j >= souvenirPrice[i - 1]) {
                    happinessCondition[i][j][0] = Math.max(happinessCondition[i - 1][j][0],
                            Math.max(happinessCondition[i - 1][j][1],
                                    happinessCondition[i - 1][j][2]));
                    happinessCondition[i][j][1] = happinessCondition[i - 1][j - souvenirPrice[i - 1]][0]
                            + souvenirHappiness[i - 1];
                    happinessCondition[i][j][2] = happinessCondition[i - 1][j - souvenirPrice[i - 1]][1]
                            + souvenirHappiness[i - 1];
                } else {
                    happinessCondition[i][j][0] = happinessCondition[i - 1][j][0];
                }
            }
        }

        if (searchPrice > maxPossible){
            searchPrice = maxPossible;
        }
        int sampleA = Math.max(happinessCondition[souvenirPrice.length][searchPrice][0],
                Math.max(happinessCondition[souvenirPrice.length][searchPrice][1],
                        happinessCondition[souvenirPrice.length][searchPrice][2]));
        for (int i = searchPrice - 1; i >= 0; i--) {
            int sampleB = Math.max(happinessCondition[souvenirPrice.length][searchPrice - 1][0],
                    Math.max(happinessCondition[souvenirPrice.length][searchPrice - 1][1],
                            happinessCondition[souvenirPrice.length][searchPrice - 1][2]));
            int result = Integer.compare(sampleA, sampleB);
            if (result != 0) {
                break;
            }
        }
        answers[index] = sampleA;

        if (queryType == 2) {
            oIndex = new StringBuilder("");
            int product = souvenirPrice.length;
            int choosenIndex = indexTracking(product, happinessCondition, souvenirPrice, searchPrice);
            while (product > 0) {
                switch (choosenIndex) {
                    case 0:
                        product-=1;
                        choosenIndex = indexTracking(product, happinessCondition, souvenirPrice, searchPrice);
                        break;
                    case 1:
                        oIndex.append(" ").append(product);
                        searchPrice -= souvenirPrice[product-1];
                        product-=1;
                        choosenIndex = 0;
                        break;
                    case 2:
                        oIndex.append(" ").append(product ).append(" ").append(product-1);
                        searchPrice -= souvenirPrice[product-1] + souvenirPrice[product - 2];
                        product -= 2;
                        choosenIndex = 0;
                        break;
                }
            }
            oIndex.append(" ").reverse();
        }
}


    public static int indexTracking(int product,int[][][] happinessCondition, int[] souvenirPrice, int searchPrice) {
        int happiness = Math.max(happinessCondition[product][searchPrice][0],
                Math.max(happinessCondition[product][searchPrice][1],
                        happinessCondition[product][searchPrice][2]));

        if (happiness == happinessCondition[product][searchPrice][0]) {
            return 0;
        } else if (happiness == happinessCondition[product][searchPrice][1]) {
            return 1;
        } else {
            return 2;
        }
    }

    public static void handleS(int[] answers, int index, int searchPrice, int[] fishPrice){
        int lowIndex = 0;
        int highIndex = fishPrice.length - 1;
        int minDifferent = Integer.MAX_VALUE;
        while (lowIndex <= highIndex){
            int mid = ( lowIndex + highIndex ) / 2;
            if (fishPrice[mid] == searchPrice){
                answers[index] = 0;
                return;
            }
            if (fishPrice[mid] < searchPrice) {
                lowIndex = mid + 1;
            } else {
                highIndex = mid - 1;
            }
            minDifferent = Math.min(minDifferent,Math.abs(searchPrice - fishPrice[mid]));
        }
        answers[index] = minDifferent;
    }

    public static void updateQueue(){
        ArrayList<Visitor> visitorList = new ArrayList<>();

        for (Visitor temp : visitorQueue){
            if (temp.patience != 1){
                temp.patience -= 1;
            } else {
                visitorList.add(temp);
            }
        }

        for (Visitor temp : visitorList){
            visitorQueue.remove(temp);
        }

    }

    // InputReader class for faster IO
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

        public int nextInt() {
            return Integer.parseInt(next());
        }

    }
}
