import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws IOException {
        System.out.println("Assignment 1 - example: expected=157, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=7826, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=70, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=2577, actual=" + calculate("mypuzzle.txt", false));
    }    

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        var rucksacks = puzzle.split("\n");
        String[][] groups;
        if (assignment1) {
            // Assignment 1: create group per rucksack, by splitting rucksack in 2 equally sized compartments
            groups = new String[rucksacks.length][2];
            for (var i = 0; i < rucksacks.length; i++) {
                var rucksack = rucksacks[i];
                var length = rucksack.length();
                groups[i][0] = rucksack.substring(0, length / 2); // compartment 1
                groups[i][1] = rucksack.substring(length / 2, length); // compartment 2
            }
        } else {
            // Assignment 2: create group per 3 rucksacks
            groups = new String[rucksacks.length / 3][3];
            var chuckSize = 3;
            var counter = 0;
            for (int i = 0; i < rucksacks.length - chuckSize + 1; i += chuckSize)
                groups[counter++] = Arrays.copyOfRange(rucksacks, i, i + chuckSize);
        }

        var prioritySum = 0;
        for (var group : groups) {
            var matchingItem = Arrays.stream(group[0].split(""))
                    .distinct()
                    .filter(c0 -> Arrays.stream(group[1].split("")).anyMatch(c1 -> c1.equals(c0)))
                    .filter(c0 -> group.length < 3 || Arrays.stream(group[2].split("")).anyMatch(c2 -> c2.equals(c0)))
                    .findFirst()
                    .get()
                    .charAt(0);
            prioritySum += (int)Character.toLowerCase(matchingItem) - 'a' + 1;
            if (Character.isUpperCase(matchingItem))
                prioritySum += 26;
        }

        return prioritySum;
    }
}