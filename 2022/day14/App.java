import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 24);
        calculate(true, "mypuzzle.txt", 638);

        calculate(false, "example.txt", 93);
        calculate(false, "mypuzzle.txt", 31722);
    }

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Add rocks to cave
        var maxY = 0;
        var cave = new HashSet<String>();
        for (var line : puzzle.lines().toList()) {
            var lineSplit = Arrays.stream(line.split(",|( -> )")).map(Integer::parseInt).collect(Collectors.toList());
            for (var i = 0; i < lineSplit.size() - 3; i += 2) {
                var sRock = new Point(lineSplit.get(i), lineSplit.get(i+1));
                var eRock = new Point(lineSplit.get(i+2), lineSplit.get(i+3));
                for (var x = sRock.x; x != eRock.x; x += (eRock.x > sRock.x ? 1 : -1))
                    cave.add(new Point(x, sRock.y).toString());
                for (var y = sRock.y; y != eRock.y; y += (eRock.y > sRock.y ? 1 : -1))
                    cave.add(new Point(sRock.x, y).toString());
                cave.add(eRock.toString());
                maxY = Math.max(Math.max(maxY, eRock.y), sRock.y);
            }
        }
        final var rockCount = cave.size();

        // Add sand to cave
        var done = false;
        while (!done) {
            var sand = new Point(500,0); // init sand to start position
            while (true) { 
                if (sand.y == maxY+1) break; // sand has reached end destination, so break out of loop
                if (!cave.contains(new Point(sand.x, sand.y+1).toString())) { sand.y++; continue; }
                if (!cave.contains(new Point(sand.x-1, sand.y+1).toString())) { sand.x--; sand.y++; continue; }
                if (!cave.contains(new Point(sand.x+1, sand.y+1).toString())) { sand.x++; sand.y++; continue; }
                break; // sand could not move, so break out of loop
            }
            // assignment1: stop when sand starts flowing (y == maxY+1), assignment2: stop when sand comes to rest (x=500&y=0)
            done = ((assignment1 && sand.y == maxY+1) || (!assignment1 && sand.x == 500 && sand.y == 0));
            if (!done || !assignment1) cave.add(sand.toString()); // final sand should only be added for assignment2
        }
        final var sandCount = cave.size() - rockCount;

        // Print results
        var actual = sandCount;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static final class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("(%s,%s)", x, y);
        }
    }

}
