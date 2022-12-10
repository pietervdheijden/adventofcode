import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", "13140");
        calculate(true, "mypuzzle.txt", "13440");

        calculate(false, "example.txt", "##..##..##..##..##..##..##..##..##..##..\n###...###...###...###...###...###...###.\n####....####....####....####....####....\n#####.....#####.....#####.....#####.....\n######......######......######......####\n#######.......#######.......#######.....");
        calculate(false, "mypuzzle.txt", "###..###..####..##..###...##..####..##..\n#..#.#..#....#.#..#.#..#.#..#....#.#..#.\n#..#.###....#..#....#..#.#..#...#..#..#.\n###..#..#..#...#.##.###..####..#...####.\n#....#..#.#....#..#.#.#..#..#.#....#..#.\n#....###..####..###.#..#.#..#.####.#..#.");
    }    

    private static void calculate(Boolean assignment1, String dataset, String expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Initialize variables
        var cycle = 0; // Use zero-based index to use modulo operation.
        var X = 1 - 1; // Decrease by 1 because of zero-based index (see previous line)
        var signalStrengthSum = 0; // Assignment 1: calculate signal strength sum
        var image = ""; // Assignment 2: render image

        // Process instructions
        for (var lines : puzzle.split("\n")) {
            var linesSplit = lines.split(" ");
            var instruction = linesSplit[0];
            var addx = instruction.equals("addx");
            var cycleCount = addx ? 2 : 1;
            while (cycleCount > 0) {
                if ((cycle+21) % 40 == 0)
                    signalStrengthSum += (cycle+1) * (X+1);
                image += ((cycle%40) >= X && (cycle%40) <= X + 2) ? "#" : ".";
                cycle++;
                if (cycle % 40 == 0)
                    image += "\n";
                cycleCount--;
            }
            if (addx)
                X += Integer.parseInt(linesSplit[1]);
        }
        image = image.replaceAll("[\n\r]$",""); // trim trailing line break

        // Print results
        var actual = assignment1 ? Integer.toString(signalStrengthSum) : image;
        var template = assignment1 ? "Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s" : "Assignment %s - dataset=%s [%s]\t: \nexpected=\n%s, \nactual=\n%s";
        System.out.println(String.format(template, 
            assignment1 ? "1" : "2",
            dataset,
            expected.replace("\n","").equals(actual.replace("\n","")) ? "PASSED" : "FAILED",
            expected,
            actual));
    }
}
