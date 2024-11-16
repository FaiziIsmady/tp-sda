import java.io.*;
import java.util.*;

public class TP2 {
    public static InputReader in;
    public static PrintWriter out;

    public static CircularLinkedList teams = new CircularLinkedList();
    public static int participantIdCounter = 1;
    public static int teamIdCounter = 1;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Jumlah tim
        int M = in.nextInt();

        // Ukuran tiap tim
        int[] teamSizes = new int[M];
        for (int i = 0; i < M; i++) {
            teamSizes[i] = in.nextInt();
        }

        // Read the initial points of all participants
        int totalParticipants = 0;
        for (int size : teamSizes) {
            totalParticipants += size;
        }
        int[] points = new int[totalParticipants];
        for (int i = 0; i < totalParticipants; i++) {
            points[i] = in.nextInt();
        }

        // Inisialisasi tim dan pemain
        int pointIndex = 0;
        for (int i = 0; i < M; i++) {
            int teamSize = teamSizes[i];
            Team team = new Team(teamIdCounter++);
            for (int j = 0; j < teamSize; j++) {
                Participant participant = new Participant(participantIdCounter++, points[pointIndex++]);
                team.addParticipant(participant);
            }
            teams.addTeam(team);
        }

        // Inisialisasi Sofita dan Penjoki
        teams.initializeSofitaAndPenjoki();

        int Q = in.nextInt();
        for (int i = 0; i < Q; i++) {
            String query = in.next();
            switch (query) {
                case "A":
                    int jumlahPeserta = in.nextInt();
                    teams.addParticipantsToSofitaTeam(jumlahPeserta);
                    break;

                case "B":
                    String extremeBound = in.next();
                    teams.processBQuery(extremeBound);
                    break;

                case "M":
                    String direction = in.next();
                    teams.moveSofita(direction);
                    break;

                case "T":
                    int senderId = in.nextInt();
                    int receiverId = in.nextInt();
                    int pointsToTransfer = in.nextInt();
                    teams.transferPoints(senderId, receiverId, pointsToTransfer);
                    break;

                case "G":
                    String position = in.next();
                    teams.createNewTeam(position);
                    break;

                case "V":
                    int participant1Id = in.nextInt();
                    int participant2Id = in.nextInt();
                    int teamId = in.nextInt();
                    int result = in.nextInt();
                    teams.simulateMatch(participant1Id, participant2Id, teamId, result);
                    break;

                case "E":
                    int minPoints = in.nextInt();
                    teams.eliminateTeams(minPoints);
                    break;

                case "U":
                    teams.countUniquePointsInSofitaTeam();
                    break;

                case "R":
                    teams.reorderTeams();
                    break;

                case "J":
                    String jDirection = in.next();
                    teams.movePenjoki(jDirection);
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
                    String line = reader.readLine();
                    if (line == null) return null;
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    return null;
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            String token = next();
            if (token == null) return -1;
            return Integer.parseInt(token);
        }
    }
}

class CircularLinkedList {
    Team head;
    Team tail;
    Team sofitaTeam;
    Team penjokiTeam;

    public CircularLinkedList() {
        head = null;
        tail = null;
        sofitaTeam = null;
        penjokiTeam = null;
    }

    // Add a new team to the end of the list
    public void addTeam(Team newTeam) {
        if (head == null) {
            head = newTeam;
            tail = newTeam;
            newTeam.next = newTeam;
            newTeam.prev = newTeam;
        } else {
            tail.next = newTeam;
            newTeam.prev = tail;
            newTeam.next = head;
            head.prev = newTeam;
            tail = newTeam;
        }
    }

    // Initialize Sofita and Penjoki positions
    public void initializeSofitaAndPenjoki() {
        // Sofita starts supervising the first team
        sofitaTeam = head;

        // Penjoki starts in the team with the lowest total points, not supervised by Sofita
        Team current = head;
        Team minTeam = null;
        int minPoints = Integer.MAX_VALUE;

        do {
            if (current != sofitaTeam && current.totalPoints < minPoints) {
                minPoints = current.totalPoints;
                minTeam = current;
            }
            current = current.next;
        } while (current != head);

        // If all teams are supervised by Sofita (unlikely), Penjoki doesn't join any team
        penjokiTeam = minTeam;
    }

    public void moveSofita(String direction) {
        if (sofitaTeam == null) {
            TP2.out.println(-1);
            return;
        }
    
        boolean validDirection = true;
        if (direction.equals("L")) {
            sofitaTeam = sofitaTeam.prev;
        } else if (direction.equals("R")) {
            sofitaTeam = sofitaTeam.next;
        } else {
            // Handle invalid direction input
            TP2.out.println(-1);
            validDirection = false;
        }
    
        if (validDirection) {
            // Check for Penjoki in the same team
            if (sofitaTeam == penjokiTeam) {
                sofitaTeam.cheaterCaughtCount++;
                movePenjokiAfterCaught();
                applyPenjokiConsequences(sofitaTeam);
            }
    
            // Now, print the team ID that Sofita ends up supervising
            if (sofitaTeam != null) {
                TP2.out.println(sofitaTeam.teamId);
            } else {
                TP2.out.println(-1);
            }
        }
    }               

    public void applyPenjokiConsequences(Team team) {
        int count = team.cheaterCaughtCount;
        if (count == 1) {
            // Remove top 3 participants
            team.removeTopParticipants(3);

            // After removing participants, check if team needs to be eliminated
            if (team.participantCount < 7) {
                eliminateTeam(team);

                // Conditional kalo yang diremove timSofita udah dihandle dieliminateTeam

                /* 
                // Check if the eliminated team was supervised by Sofita
                if (team == sofitaTeam) {
                    sofitaTeam = findTeamWithHighestPoints();
                    // Only output if there is no team left for Sofita
                    if (sofitaTeam == null) {
                        TP2.out.println(-1);
                    }
                    // No output if Sofita supervises a new team
                }
                */

            } else {
                // Reorder teams since totalPoints and participantCounts have changed
                reorderTeamsAfterConsequences();
                // Do not output the team ID here
            }

        } else if (count == 2) {
            // Set all participants' points to 1
            team.setAllParticipantsPoints(1);

            // Reorder teams since totalPoints have changed
            reorderTeamsAfterConsequences();
            // Do not output the team ID here
        } else if (count == 3) {
            // Eliminate the team
            eliminateTeam(team);

            // Conditional kalo yang diremove timSofita udah dihandle dieliminateTeam

            /* 
            // Check if the eliminated team was supervised by Sofita
            if (team == sofitaTeam) {
                sofitaTeam = findTeamWithHighestPoints();
                // Only output if there is no team left for Sofita
                if (sofitaTeam == null) {
                    TP2.out.println(-1);
                }
                // No output if Sofita supervises a new team
            } 
            */
            // Reorder teams since teams have changed
            reorderTeamsAfterConsequences();
            // Do not output the team ID here
        }
        // Do not output the team ID here unless the team was eliminated and Sofita has no team left
    }

    // Move Penjoki after being caught
    public void movePenjokiAfterCaught() {
        // Penjoki is thrown out and moves to the team with the lowest total points not supervised by Sofita
        Team current = head;
        Team minTeam = null;
        int minPoints = Integer.MAX_VALUE;

        if (current == null) {
            penjokiTeam = null;
            return;
        }

        do {
            if (current != sofitaTeam && current.totalPoints < minPoints) { // Removed "current != penjokiTeam"
                minPoints = current.totalPoints;
                minTeam = current;
            }
            current = current.next;
        } while (current != head);

        // If no valid team found, set Penjoki to null
        penjokiTeam = minTeam;
    }

    public void eliminateTeam(Team team) {
        // Remove the team from the linked list
        if (team == head && team == tail) {
            // Only one team left
            head = null;
            tail = null;
        } else if (team.next == team.prev) {
            // Only two teams left, and one is being eliminated
            head = team.next;
            tail = team.next;
            team.next.next = team.next;
            team.next.prev = team.next;
        } else {
            team.prev.next = team.next;
            team.next.prev = team.prev;
            if (team == head) {
                head = team.next;
            }
            if (team == tail) {
                tail = team.prev;
            }
        }
    
        // Remove Penjoki's reference if necessary
        if (team == penjokiTeam) {
            penjokiTeam = null;
            movePenjokiAfterCaught();
        }

        if (team == sofitaTeam) {
            sofitaTeam = findTeamWithHighestPoints();
            if (sofitaTeam == null) {
                TP2.out.println(-1);
            }
            // No output if Sofita supervises a new team
        }
    }
    
    public Team findTeamWithHighestPoints() {
        if (head == null) return null;
        Team current = head;
        Team maxTeam = head;
        do {
            if (compareTeams(current, maxTeam) < 0) {
                maxTeam = current;
            }
            current = current.next;
        } while (current != head);
        return maxTeam;
    }        

    // QUERY A Menambah peserta pada tim yang sedang dawasi Sofita
    public void addParticipantsToSofitaTeam(int jumlahPeserta) {
        if (sofitaTeam == null) { // Checker jika Sofita ga ngawasin tim apapun (Jumlah tim = 0)
            TP2.out.println(-1);
            return;
        }
        for (int i = 0; i < jumlahPeserta; i++) { // For loop sebanyak jumlah peserta pada A jumlahPeserta
            Participant participant = new Participant(TP2.participantIdCounter++, 3);
            sofitaTeam.addParticipant(participant);
        }
        TP2.out.println(sofitaTeam.participantCount);
    }

    // Process query B (Extreme Bound)
    public void processBQuery(String extremeBound) {
        if (sofitaTeam == null) {
            TP2.out.println(-1);
            return;
        }
        int result = sofitaTeam.calculateExtremeParticipants(extremeBound);
        TP2.out.println(result);
    }

    // Transfer points between participants in the supervised team
    public void transferPoints(int senderId, int receiverId, int pointsToTransfer) {
        if (sofitaTeam == null) {
            TP2.out.println(-1);
            return;
        }
        boolean success = sofitaTeam.transferPoints(senderId, receiverId, pointsToTransfer);
        if (!success) {
            TP2.out.println(-1);
        }
    }


    // Membuat tim baru
    public void createNewTeam(String position) {
        // Validate position input
        if (!position.equals("L") && !position.equals("R")) {
            TP2.out.println(-1);
            return;
        }

        // Create a new team with seven participants, each with 1 point
        Team newTeam = new Team(TP2.teamIdCounter++);
        for (int i = 0; i < 7; i++) { // Tim terinisialisasi berisi 7 orang
            Participant participant = new Participant(TP2.participantIdCounter++, 1);
            newTeam.addParticipant(participant);
        }

        if (position.equals("L")) {
            // Insert to the left of Sofita's team
            if (sofitaTeam == null) {
                addTeam(newTeam);
                // After adding, set Sofita to supervise the team with highest points
                sofitaTeam = findTeamWithHighestPoints();
            } else {
                Team leftTeam = sofitaTeam.prev;

                leftTeam.next = newTeam;
                newTeam.prev = leftTeam;
                newTeam.next = sofitaTeam;
                sofitaTeam.prev = newTeam;

                if (sofitaTeam == head) {
                    head = newTeam;
                }
            }

        } else if (position.equals("R")) {
            // Insert to the right of Sofita's team
            if (sofitaTeam == null) {
                addTeam(newTeam);
                // After adding, set Sofita to supervise the team with highest points
                sofitaTeam = findTeamWithHighestPoints();
            } else {
                Team rightTeam = sofitaTeam.next;

                sofitaTeam.next = newTeam;
                newTeam.prev = sofitaTeam;
                newTeam.next = rightTeam;
                rightTeam.prev = newTeam;

                if (sofitaTeam == tail) {
                    tail = newTeam;
                }
            }
        }

        // Output the new team's ID
        TP2.out.println(newTeam.teamId);
    }


    public void simulateMatch(int participant1Id, int participant2Id, int teamId, int result) {
        if (sofitaTeam == null) {
            TP2.out.println(-1);
            return;
        }
        
        Participant participant1 = sofitaTeam.findParticipant(participant1Id);
        if (participant1 == null) {
            TP2.out.println(-1);
            return;
        }

        Team otherTeam = findTeamById(teamId);
        if (otherTeam == null) {
            TP2.out.println(-1);
            return;
        }

        Participant participant2 = otherTeam.findParticipant(participant2Id);
        if (participant2 == null) {
            TP2.out.println(-1);
            return;
        }
    
        participant1.matches++;
        participant2.matches++;
    
        int participant1OldPoints = participant1.points;
        int participant2OldPoints = participant2.points;
    
        if (result == 0) {
            participant1.points += 1;
            participant2.points += 1;
    
            // Update team's totalPoints
            sofitaTeam.totalPoints += 1;
            otherTeam.totalPoints += 1;
    
            sofitaTeam.updateParticipant(participant1);
            otherTeam.updateParticipant(participant2);
    
            TP2.out.println(participant1.points + " " + participant2.points);
            
        } else if (result == 1) {
            participant1.points += 3;
            participant2.points -= 3;
            if (participant2.points < 0) participant2.points = 0;
    
            // Update team's totalPoints
            sofitaTeam.totalPoints += (participant1.points - participant1OldPoints);
            otherTeam.totalPoints += (participant2.points - participant2OldPoints);
    
            sofitaTeam.updateParticipant(participant1);
            otherTeam.updateParticipant(participant2);
    
            if (participant2.points == 0) {
                otherTeam.removeParticipant(participant2);
                if (otherTeam.participantCount < 7) {
                    eliminateTeam(otherTeam);
                }
            }
            TP2.out.println(participant1.points);

        } else if (result == -1) {
            participant2.points += 3;
            participant1.points -= 3;
            if (participant1.points < 0) participant1.points = 0;
    
            // Update team's totalPoints
            sofitaTeam.totalPoints += (participant1.points - participant1OldPoints);
            otherTeam.totalPoints += (participant2.points - participant2OldPoints);
    
            sofitaTeam.updateParticipant(participant1);
            otherTeam.updateParticipant(participant2);
    
            if (participant1.points == 0) {
                sofitaTeam.removeParticipant(participant1);
                // Check if sofitaTeam needs to be eliminated
                if (sofitaTeam.participantCount < 7) {
                    eliminateTeam(sofitaTeam);
                    // Update sofitaTeam reference
                    sofitaTeam = findTeamWithHighestPoints();
                    if (sofitaTeam == null) {
                        TP2.out.println(-1);
                        return;
                    }
                }
            }
            TP2.out.println(participant2.points);
        }
    }
    
    // Find a team by its ID
    public Team findTeamById(int teamId) {
        if (head == null) return null;
        Team current = head;
        do {
            if (current.teamId == teamId) {
                return current;
            }
            current = current.next;
        } while (current != head);
        return null;
    }

    // Eliminate teams with total points less than minPoints
    public void eliminateTeams(int minPoints) {
        if (head == null) {
            TP2.out.println(0);
            return;
        }
        int eliminatedCount = 0;
        List<Team> teamsToEliminate = new ArrayList<>();
        Team current = head;
        do { // Memasukkan tim ke arraylist untuk di evaluasi
            if (current.totalPoints < minPoints) {
                teamsToEliminate.add(current);
            }
            current = current.next;
        } while (current != head);

        for (Team team : teamsToEliminate) {
            eliminateTeam(team);
            eliminatedCount++;
        }

        TP2.out.println(eliminatedCount);
    }

    // Count unique points in Sofita's team
    public void countUniquePointsInSofitaTeam() {
        if (sofitaTeam == null) {
            TP2.out.println(-1);
            return;
        }
        int uniqueCount = sofitaTeam.countUniquePoints();
        TP2.out.println(uniqueCount);
    }

    public void reorderTeams() {
        if (head == null) {
            TP2.out.println(-1);
            return;
        }
        // Collect teams into a list
        List<Team> teamList = new ArrayList<>();
        Team current = head;
        do { // Memasukkan node tim ke arraylist baru
            teamList.add(current);
            current = current.next;
        } while (current != head);
    
        // Custom sort the team list
        customSortTeams(teamList);
    
        // Rebuild the circular linked list
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (i == 0) {
                head = team;
                team.prev = teamList.get(teamList.size() - 1);
            } else {
                team.prev = teamList.get(i - 1);
            }
            if (i == teamList.size() - 1) {
                tail = team;
                team.next = teamList.get(0);
            } else {
                team.next = teamList.get(i + 1);
            }
        }
    
        // Update Sofita's position to the team with highest points
        sofitaTeam = findTeamWithHighestPoints();

        // Check if Penjoki is in the same team as Sofita
        if (penjokiTeam != null && sofitaTeam == penjokiTeam) {
            sofitaTeam.cheaterCaughtCount++;
            movePenjokiAfterCaught();
            applyPenjokiConsequences(sofitaTeam);
            // Do not output the team ID again here
        }
    
        // Output the new team ID supervised by Sofita before checking for Penjoki
        if (sofitaTeam != null) {
            TP2.out.println(sofitaTeam.teamId);
        } else {
            TP2.out.println(-1);
        }
    }       
    
    // Custom sort teams based on total points, participant count, team ID
    public void customSortTeams(List<Team> teamList) {
        // Implement a simple sorting algorithm (insertion sort)
        for (int i = 1; i < teamList.size(); i++) {
            Team key = teamList.get(i);
            int j = i - 1;
            while (j >= 0 && compareTeams(teamList.get(j), key) > 0) { // Changed < 0 to > 0
                teamList.set(j + 1, teamList.get(j));
                j--;
            }
            teamList.set(j + 1, key);
        }
    }


    public int compareTeams(Team a, Team b) {
        if (a.totalPoints != b.totalPoints) {
            return Integer.compare(b.totalPoints, a.totalPoints); // Descending order
        } else if (a.participantCount != b.participantCount) {
            return Integer.compare(a.participantCount, b.participantCount); // Ascending order
        } else {
            return Integer.compare(a.teamId, b.teamId); // Ascending order
        }
    }    

    // Move Penjoki to the left or right team
    public void movePenjoki(String direction) {
        if (penjokiTeam == null) {
            TP2.out.println(-1);
            return;
        }

        Team targetTeam;
        if (direction.equals("L")) {
            targetTeam = penjokiTeam.prev;
        } else if (direction.equals("R")) {
            targetTeam = penjokiTeam.next;
        } else {
            targetTeam = penjokiTeam;
        }

        // Penjoki can detect if Sofita is supervising the target team
        if (targetTeam == sofitaTeam) {
            // Penjoki stays in the current team
            // Do nothing
        } else {
            // Penjoki moves to the target team
            penjokiTeam = targetTeam;
        }

        if (penjokiTeam == null) {
            TP2.out.println(-1);
            return;
        }

        TP2.out.println(penjokiTeam.teamId);
    }

    public void reorderTeamsAfterConsequences() {
        if (head == null) {
            return; // No output needed
        }
        // Collect teams into a list
        List<Team> teamList = new ArrayList<>();
        Team current = head;
        do {
            teamList.add(current);
            current = current.next;
        } while (current != head);
    
        // Custom sort the team list
        customSortTeams(teamList);
    
        // Rebuild the circular linked list
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (i == 0) {
                head = team;
                team.prev = teamList.get(teamList.size() - 1);
            } else {
                team.prev = teamList.get(i - 1);
            }
            if (i == teamList.size() - 1) {
                tail = team;
                team.next = teamList.get(0);
            } else {
                team.next = teamList.get(i + 1);
            }
        }
    }
}

class Team {
    int teamId;
    AVLTree participants;
    int totalPoints;
    int participantCount;
    int cheaterCaughtCount;

    Map<Integer, Participant> participantMap;

    Team next;
    Team prev;

    public Team(int teamId) {
        this.teamId = teamId;
        this.participants = new AVLTree();
        this.totalPoints = 0;
        this.participantCount = 0;
        this.cheaterCaughtCount = 0;
        this.participantMap = new HashMap<>();
    }

    // Add a participant to the team
    public void addParticipant(Participant participant) {
        participants.insert(participant);
        participantMap.put(participant.id, participant);
        totalPoints += participant.points;
        participantCount++;
    }

    // Remove a participant from the team
    public void removeParticipant(Participant participant) {
        participants.delete(participant);
        participantMap.remove(participant.id);
        totalPoints -= participant.points;
        participantCount--;
    }

    // Update a participant's data in the AVL tree
    public void updateParticipant(Participant participant) {
        participants.delete(participant); // Remove old data
        participants.insert(participant); // Re-insert with updated data
        // No need to update totalPoints here as it remains the same
    }

    // Find a participant by ID
    public Participant findParticipant(int participantId) {
        return participantMap.get(participantId); // O(1) lookup
    }

    // Remove top N participants based on points
    public void removeTopParticipants(int n) {
        List<Participant> topParticipants = participants.getTopNParticipants(n);
        for (Participant p : topParticipants) {
            removeParticipant(p);
        }
    }

    // Set all participants' points to a specific value
    public void setAllParticipantsPoints(int value) {
        List<Participant> allParticipants = participants.getAllParticipants();
        totalPoints = 0;
        for (Participant p : allParticipants) {
            p.points = value;
            totalPoints += value;
        }
        // Rebuild the AVL tree as the ordering might change
        participants.rebuildTree(allParticipants);
    }

    // Count unique points in the team
    public int countUniquePoints() {
        Set<Integer> uniquePoints = participants.getUniquePoints();
        return uniquePoints.size();
    }

    // Calculate participants based on extreme bound
    public int calculateExtremeParticipants(String extremeBound) {
        return participants.calculateExtremeParticipants(extremeBound);
    }

    // Transfer points between participants in the team
    public boolean transferPoints(int senderId, int receiverId, int pointsToTransfer) {
        Participant sender = findParticipant(senderId);
        Participant receiver = findParticipant(receiverId);

        // Check if both participants exist
        if (sender == null || receiver == null) {
            return false;
        }

        // Check if the sender has enough points
        if (pointsToTransfer >= sender.points) {
            return false;
        }

        // Transfer points
        sender.points -= pointsToTransfer;
        receiver.points += pointsToTransfer;

        // Update participants in the AVL tree
        updateParticipant(sender);
        updateParticipant(receiver);

        // Print the updated points
        TP2.out.println(sender.points + " " + receiver.points);

        return true;
    }
}

class Participant {
    int id;
    int points;
    int matches;

    public Participant(int id, int points) {
        this.id = id;
        this.points = points;
        this.matches = 0;
    }
}

class AVLTree {
    class Node {
        Participant participant;
        Node left, right;
        int height;

        public Node(Participant participant) {
            this.participant = participant;
            this.height = 1;
        }
    }

    public Node root;

    // Insert a participant into the AVL tree
    public void insert(Participant participant) {
        root = insertRec(root, participant);
    }

    public Node insertRec(Node node, Participant participant) {
        if (node == null) {
            return new Node(participant);
        }

        int cmp = compareParticipants(participant, node.participant);
        if (cmp < 0) {
            node.left = insertRec(node.left, participant);
        } else {
            node.right = insertRec(node.right, participant);
        }

        updateHeight(node);
        return balance(node);
    }

    // Delete a participant from the AVL tree
    public void delete(Participant participant) {
        root = deleteRec(root, participant);
    }

    public Node deleteRec(Node node, Participant participant) {
        if (node == null) return null;

        int cmp = compareParticipants(participant, node.participant);
        if (cmp < 0) {
            node.left = deleteRec(node.left, participant);
        } else if (cmp > 0) {
            node.right = deleteRec(node.right, participant);
        } else {
            // Found the node to delete
            if (node.left == null || node.right == null) {
                Node temp = null;
                if (node.left != null) temp = node.left;
                else if (node.right != null) temp = node.right;

                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }
            } else {
                Node temp = getMinValueNode(node.right);
                node.participant = temp.participant;
                node.right = deleteRec(node.right, temp.participant);
            }
        }

        if (node == null) return node;

        updateHeight(node);
        return balance(node);
    }

    // Find a participant by ID
    public Participant find(int participantId) {
        return findRec(root, participantId);
    }

    public Participant findRec(Node node, int participantId) {
        if (node == null) return null;
        if (node.participant.id == participantId) return node.participant;

        Participant leftResult = findRec(node.left, participantId);
        if (leftResult != null) return leftResult;

        return findRec(node.right, participantId);
    }

    // Get top N participants
    public List<Participant> getTopNParticipants(int n) {
        List<Participant> result = new ArrayList<>();
        getTopNRec(root, result, n);
        return result;
    }

    public void getTopNRec(Node node, List<Participant> result, int n) {
        if (node == null || result.size() >= n) return;
        getTopNRec(node.right, result, n);
        if (result.size() < n) {
            result.add(node.participant);
            getTopNRec(node.left, result, n);
        }
    }

    // Get all participants
    public List<Participant> getAllParticipants() {
        List<Participant> result = new ArrayList<>();
        getAllRec(root, result);
        return result;
    }

    public void getAllRec(Node node, List<Participant> result) {
        if (node == null) return;
        getAllRec(node.right, result);
        result.add(node.participant);
        getAllRec(node.left, result);
    }

    // Rebuild the AVL tree from a list of participants
    public void rebuildTree(List<Participant> participants) {
        root = null;
        for (Participant p : participants) {
            insert(p);
        }
    }

    // Get unique points in the tree
    public Set<Integer> getUniquePoints() {
        Set<Integer> uniquePoints = new HashSet<>();
        collectUniquePoints(root, uniquePoints);
        return uniquePoints;
    }

    public void collectUniquePoints(Node node, Set<Integer> uniquePoints) {
        if (node == null) return;
        collectUniquePoints(node.left, uniquePoints);
        uniquePoints.add(node.participant.points);
        collectUniquePoints(node.right, uniquePoints);
    }

    public int calculateExtremeParticipants(String extremeBound) {
        // Collect all points into a list
        List<Integer> pointsList = new ArrayList<>();
        collectPoints(root, pointsList);
        sortPointsList(pointsList); // Implementasi method sorting
    
        if (pointsList.isEmpty()) return 0;
    
        int K = pointsList.size();
    
        // Calculate IndexQ1 and IndexQ3 using the new formulas
        int indexQ1 = Math.max(0, (int) Math.floor((K - 1) * 0.25));
        int indexQ3 = Math.min(K - 1, (int) Math.floor((K - 1) * 0.75));
    
        int Q1 = pointsList.get(indexQ1);
        int Q3 = pointsList.get(indexQ3);
        int IQR = Q3 - Q1;
        int L = Q1 - (int) Math.floor(1.5 * IQR);
        int U = Q3 + (int) Math.floor(1.5 * IQR);
    
        int count = 0;
        if (extremeBound.equals("U")) {
            // Count participants with points > U
            for (int point : pointsList) {
                if (point > U) count++;
            }
        } else if (extremeBound.equals("L")) {
            // Count participants with points < L
            for (int point : pointsList) {
                if (point < L) count++;
            }
        }
        return count;
    }    

    public void collectPoints(Node node, List<Integer> pointsList) {
        if (node == null) return;
        collectPoints(node.left, pointsList);
        pointsList.add(node.participant.points);
        collectPoints(node.right, pointsList);
    }

    // AVL Tree utility methods
    public void updateHeight(Node node) {
        node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));
    }

    public int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }

    public int getBalance(Node node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    public Node balance(Node node) {
        int balance = getBalance(node);
        if (balance > 1) {
            if (getBalance(node.left) >= 0) {
                // Left Left Case
                return rightRotate(node);
            } else {
                // Left Right Case
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        } else if (balance < -1) {
            if (getBalance(node.right) <= 0) {
                // Right Right Case
                return leftRotate(node);
            } else {
                // Right Left Case
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
        }
        return node;
    }

    public Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        // Perform rotation
        x.right = y;
        y.left = T2;
        // Update heights
        updateHeight(y);
        updateHeight(x);
        // Return new root
        return x;
    }

    public Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        // Perform rotation
        y.left = x;
        x.right = T2;
        // Update heights
        updateHeight(x);
        updateHeight(y);
        // Return new root
        return y;
    }

    public int compareParticipants(Participant a, Participant b) {
        if (a.points != b.points) {
            return Integer.compare(a.points, b.points); // Higher points considered greater
        } else if (a.matches != b.matches) {
            return Integer.compare(b.matches, a.matches); // Fewer matches considered greater
        } else {
            return Integer.compare(b.id, a.id); // Smaller IDs considered greater
        }
    }    

    public Node getMinValueNode(Node node) {
        Node current = node;
        while (current.left != null) current = current.left;
        return current;
    }

    public void sortPointsList(List<Integer> pointsList) {
        if (pointsList.size() <= 1) return;
        mergeSort(pointsList, 0, pointsList.size() - 1);
    }
    
    public void mergeSort(List<Integer> list, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);
            merge(list, left, mid, right);
        }
    }
    
    public void merge(List<Integer> list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
    
        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];
    
        for (int i = 0; i < n1; i++)
            leftArray[i] = list.get(left + i);
        for (int i = 0; i < n2; i++)
            rightArray[i] = list.get(mid + 1 + i);
    
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray[i] <= rightArray[j]) {
                list.set(k, leftArray[i]);
                i++;
            } else {
                list.set(k, rightArray[j]);
                j++;
            }
            k++;
        }
        while (i < n1) {
            list.set(k, leftArray[i]);
            i++;
            k++;
        }
        while (j < n2) {
            list.set(k, rightArray[j]);
            j++;
            k++;
        }
    }
}
