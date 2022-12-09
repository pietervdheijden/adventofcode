import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 13);
        calculate(true, "mypuzzle.txt", 6464);
        calculate(false, "example.txt", 1);
        calculate(false, "example2.txt", 36);
        calculate(false, "mypuzzle.txt", 2604);
    }

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Initialize knots and tailPositions
        var knots = new Knot[assignment1 ? 2 : 10];
        for (var i = 0; i < knots.length; i++)
            knots[i] = new Knot();
        var tailPositions = new ArrayList<String>();
        tailPositions.add(knots[knots.length-1].getPosition());

        // Execute motions
        for (var motion : puzzle.split("\n")) {
            var motionSplit = motion.split(" ");
            var direction = motionSplit[0].charAt(0);
            var steps = Integer.parseInt(motionSplit[1]);
            while (steps > 0) {
                for (var i = 0; i < knots.length; i++) {
                    var knot = knots[i];
                    if (i == 0) { // Head knot - simply move 1 step in direction
                        if (direction == 'U') // Up
                            knot.y++;
                        if (direction == 'D') // Down
                            knot.y--;
                        if (direction == 'L') // Left
                            knot.x--;
                        if (direction == 'R') // Right
                            knot.x++;
                    } else { // 2nd to last knot - move if either x or y (or both!) differ two positions compared to the previous knot
                        var xdiff = knots[i-1].x - knot.x;
                        var ydiff = knots[i-1].y - knot.y;
                        if (Math.abs(xdiff) == 2 || Math.abs(ydiff) == 2) {
                            if (xdiff > 0)
                                knot.x++;
                            if (xdiff < 0)
                                knot.x--;
                            if (ydiff > 0)
                                knot.y++;
                            if (ydiff < 0)
                                knot.y--;
                        }
                    }
                }
                var tailPosition = knots[knots.length-1].getPosition();
                if (!tailPositions.contains(tailPosition))
                    tailPositions.add(tailPosition);
                steps--;
            }
        }

        // Print results
        var actual = tailPositions.size();
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static class Knot {
        public int x = 0;
        public int y = 0;

        public String getPosition() {
            return String.format("(%s,%s)", x, y);
        }
    }
}
