import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String args[]) throws IOException {
        System.out.println("Assignment 1 - example: expected=2, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=569, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=4, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=936, actual=" + calculate("mypuzzle.txt", false));
    }    

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        var overlapCount = 0;
        for (var pair : puzzle.split("\n")) {
            var elves = pair.split(",");
            var elf1 = elves[0].split("-");
            var elf2 = elves[1].split("-");
            var elf1Low = Integer.parseInt(elf1[0]);
            var elf1High = Integer.parseInt(elf1[1]);
            var elf2Low = Integer.parseInt(elf2[0]);
            var elf2High = Integer.parseInt(elf2[1]);

            if (assignment1) {
                // Assignment 1: count full overlaps
                if ((elf1Low <= elf2Low && elf1High >= elf2High) || (elf2Low <= elf1Low && elf2High >= elf1High))
                    overlapCount++;
            } else {
                // Assignment 2: count partial overlaps
                if ((elf1High >= elf2Low && elf1High <= elf2High) || (elf2High >= elf1Low && elf2High <= elf1High))
                    overlapCount++;
            }
        }
        return overlapCount;
    }
}