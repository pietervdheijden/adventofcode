import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class App {
    public static void main(String[] args) throws IOException {
        System.out.println("Assignment 1 - example: expected=24000, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=69626, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=45000, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=206780, actual=" + calculate("mypuzzle.txt", false));
    }

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        var elves = new ArrayList<Integer>();
        for (var elf : puzzle.split("\n\n")) {
            var calories = Arrays.stream(elf.split("\n")).mapToInt(Integer::parseInt).sum();
            elves.add(calories);
        }

        var limit = assignment1 ? 1 : 3;
        return elves.stream().sorted(Collections.reverseOrder()).limit(limit).reduce(0, Integer::sum);
    }
}