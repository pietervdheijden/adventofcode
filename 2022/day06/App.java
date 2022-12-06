import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String args[]) throws IOException {
        System.out.println("Assignment 1 - example: expected=7, actual=" + calculate("example1.txt", true));
        System.out.println("Assignment 1 - example: expected=5, actual=" + calculate("example2.txt", true));
        System.out.println("Assignment 1 - example: expected=6, actual=" + calculate("example3.txt", true));
        System.out.println("Assignment 1 - example: expected=10, actual=" + calculate("example4.txt", true));
        System.out.println("Assignment 1 - example: expected=11, actual=" + calculate("example5.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=1300, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=19, actual=" + calculate("example1.txt", false));
        System.out.println("Assignment 2 - example: expected=23, actual=" + calculate("example2.txt", false));
        System.out.println("Assignment 2 - example: expected=23, actual=" + calculate("example3.txt", false));
        System.out.println("Assignment 2 - example: expected=29, actual=" + calculate("example4.txt", false));
        System.out.println("Assignment 2 - example: expected=26, actual=" + calculate("example5.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=3986, actual=" + calculate("mypuzzle.txt", false));
    }    

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Find first substring with x distinct characters, and return the index of last character
        var distinctCount = assignment1 ? 4 : 14;
        for (var i = 0; i < puzzle.length() - distinctCount - 1; i++) {
            if (puzzle.substring(i, i + distinctCount).chars().distinct().count() == distinctCount)
                return i + distinctCount;
        }

        // Puzzle could not be solved
        return -1;
    }
}
