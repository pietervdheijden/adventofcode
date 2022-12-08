import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 21);
        calculate(true, "mypuzzle.txt", 1715);
        calculate(false, "example.txt", 8);
        calculate(false, "mypuzzle.txt", 374400);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Read trees
        var rows = puzzle.split("\n");
        var trees = new int[rows.length][];
        for (var i = 0; i < rows.length; i++)
            trees[i] = Stream.of(rows[i].split("")).mapToInt(Integer::parseInt).toArray();

        // Calculate
        // Assignment 1: calculate visible count
        // Assignment 2: calculate max scenic score
        var visibleCount = 0;
        var maxScenicScore = 0;
        for (var row = 0; row < trees.length; row++) {
            for (var column = 0; column < trees[row].length; column++) {
                // Fill arrays visible and viewingDistances
                Boolean[] visible = {true, true, true, true}; // 0=up,1=down,2=left,3=right
                int[] viewingDistances = {0, 0, 0, 0}; // 0=up,1=down,2=left,3=right
                for (var otherRow = row-1; otherRow >= 0; otherRow--) { // check up
                    viewingDistances[0]++;
                    if (trees[row][column] <= trees[otherRow][column]) {
                        visible[0] = false;
                        break;
                    }
                }
                for (var otherRow = row+1; otherRow < trees.length; otherRow++) { // check down
                    viewingDistances[1]++;
                    if (trees[row][column] <= trees[otherRow][column]) {
                        visible[1] = false;
                        break;
                    }
                }
                for (var otherColumn = column-1; otherColumn >= 0; otherColumn--) { // check left
                    viewingDistances[2]++;
                    if (trees[row][column] <= trees[row][otherColumn]) {
                        visible[2] = false;
                        break;
                    }
                }
                for (var otherColumn = column+1; otherColumn < trees[row].length; otherColumn++) { // check right
                    viewingDistances[3]++;
                    if (trees[row][column] <= trees[row][otherColumn]) {
                        visible[3] = false;
                        break;
                    }
                }

                // Update visibleCount and maxScenicScore
                if (Arrays.stream(visible).anyMatch(b -> b))
                    visibleCount++;
                var scenicScore = Arrays.stream(viewingDistances).reduce(1, (a, b) -> a * b);
                maxScenicScore = Math.max(maxScenicScore, scenicScore);
            }
        }

        // Print results
        var actual = assignment1 ? visibleCount : maxScenicScore;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
        
    }
}
