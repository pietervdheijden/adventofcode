import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Assignment 1 - example: expected=15, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=13009, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=12, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=10398, actual=" + calculate("mypuzzle.txt", false));
    }

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        var score = 0;
        var map = new HashMap<String, Integer>() {{
            put("A", 1);
            put("B", 2);
            put("C", 3);
            put("X", 1);
            put("Y", 2);
            put("Z", 3);
        }};
        for (var round : puzzle.split("\n")) {
            var shapes = round.split(" ");
            var player1 = map.get(shapes[0]); // A = Rock, B = Paper, C = Scissors
            var player2 = map.get(shapes[1]); // Assignment 1: X = Rock, Y = Paper, Z = Scissors
                                              // Assignment 2: X = Loss, Y = Draw,  Z = Win
            if (assignment1) {
                // Add score for outcome
                if (player1 == player2) score += 3;
                if (player1+1 == player2) score += 6;
                if (player1-2 == player2) score += 6; 

                // Add score for shape
                score += player2;
            } else {
                // Add score for outcome
                if (player2 == 1) score += 0;
                if (player2 == 2) score += 3;
                if (player2 == 3) score += 6;

                // Add score for shape
                int shape = -1;
                if (player2 == 1) shape = player1 - 1;
                if (player2 == 2) shape = player1;
                if (player2 == 3) shape = player1 + 1;
                if (shape == 0) shape = 3; // rotate to end: scissors loses to rock
                if (shape == 4) shape = 1; // rotate to begin: rock beats scissors
                score += shape;                
            }
        }
        return score;
    }
}