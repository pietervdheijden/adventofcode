import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        System.out.println("Assignment 1 - example: expected=CMZ, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=HBTMTBSDC, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=MCD, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=PQTJRSHWS, actual=" + calculate("mypuzzle.txt", false));
    }    

    private static String calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));
        var puzzleParts = puzzle.split("\n\n");

        // Read stacks
        var stackLines = puzzleParts[0].split("\n");
        var stackCount = stackLines[stackLines.length - 1].split("   ").length; // use last line to determine stack count
        var stacks = new ArrayList<Stack<String>>();
        for (var column = 0; column < stackCount; column++) {
            stacks.add(new Stack<String>());
            for (var row = stackLines.length-2; row >= 0; row--) { // read from bottom to top, to fill the stack in the correct order
                var crate = stackLines[row].substring(4*column, 4*column + 3).replaceAll("\\[([A-Z])\\]", "$1");
                if (!crate.isBlank())
                    stacks.get(column).push(crate);
            }
        }

        // Execute procedure
        var matcher = Pattern.compile("move ([0-9]+) from ([0-9]+) to ([0-9]+)").matcher(puzzleParts[1]);
        while (matcher.find()) {
            var crateCount = Integer.parseInt(matcher.group(1));
            var fromStack = Integer.parseInt(matcher.group(2));
            var toStack = Integer.parseInt(matcher.group(3));
            var crates = new String[crateCount];
            for (var i = 0; i < crateCount; i++)
                crates[i] = stacks.get(fromStack-1).pop();
            if (assignment1) {
                for (var i = 0; i < crates.length; i++)
                    stacks.get(toStack-1).push(crates[i]);
            } else { // Assignment 2: retain original stack order by pushing crates in reverse order
                for (var i = crates.length-1; i >= 0; i--)
                    stacks.get(toStack-1).push(crates[i]);
            }
        }

        // Read top crates
        var message = "";
        for (var stack : stacks)
            message += stack.pop();
        return message;
    }
}
