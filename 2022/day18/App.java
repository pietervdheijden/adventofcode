import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example1.txt", 10);
        calculate(true, "example2.txt", 64);
        calculate(true, "mypuzzle.txt", 3500);
        calculate(false, "example2.txt", 58);
        calculate(false, "mypuzzle.txt", 2048);
    }

    // Solution is inspired by: https://github.com/encse/adventofcode/blob/master/2022/Day18/Solution.cs
    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse lava
        var lava = puzzle.lines().map(l -> l.split(",")).map(l -> new Cube(Integer.parseInt(l[0]), Integer.parseInt(l[1]), Integer.parseInt(l[2]))).collect(Collectors.toSet());
        final var minX = lava.stream().mapToInt(c -> c.x).min().getAsInt() - 1;
        final var maxX = lava.stream().mapToInt(c -> c.x).max().getAsInt() + 1;
        final var minY = lava.stream().mapToInt(c -> c.y).min().getAsInt() - 1;
        final var maxY = lava.stream().mapToInt(c -> c.y).max().getAsInt() + 1;
        final var minZ = lava.stream().mapToInt(c -> c.z).min().getAsInt() - 1;
        final var maxZ = lava.stream().mapToInt(c -> c.z).max().getAsInt() + 1;   
        final var minCube = new Cube(minX, minY, minZ);
        final var maxCube = new Cube(maxX, maxY, maxZ);

        // Fill water
        var water = new HashSet<Cube>();
        var queue = new LinkedList<Cube>();
        water.add(minCube);
        queue.add(minCube);
        while (!queue.isEmpty()) {
            var cube = queue.removeFirst();
            for (var neighbor : getNeighbors(cube, minCube, maxCube)) {
                if (!lava.contains(neighbor) && !water.contains(neighbor)) {
                    water.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Calculate surface area
        var surfaceArea = lava
                .stream()
                .map(l -> getNeighbors(l, minCube, maxCube))
                .flatMap(List::stream)
                .toList()
                .stream()
                .filter(n -> 
                    (assignment1 && !lava.contains(n)) || // assignment 1: calculate surface area by counting neighbors which are not lava
                    (!assignment1 && water.contains(n)))  // assignment 2: calculate ext surface area by counting neighbors which are water
                .count();

        // Print results
        var actual = surfaceArea;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static List<Cube> getNeighbors(Cube cube, Cube minCube, Cube maxCube) {
        return List
                .of(new Cube(cube.x+1, cube.y, cube.z),
                    new Cube(cube.x-1, cube.y, cube.z),
                    new Cube(cube.x, cube.y+1, cube.z),
                    new Cube(cube.x, cube.y-1, cube.z),
                    new Cube(cube.x, cube.y, cube.z+1),
                    new Cube(cube.x, cube.y, cube.z-1))
                .stream()
                .filter(c -> 
                    c.x >= minCube.x && c.y >= minCube.y && c.z >= minCube.z &&
                    c.x <= maxCube.x && c.y <= maxCube.y && c.z <= maxCube.z)
                .toList();
    }

    private static class Cube {
        private final int x;
        private final int y;
        private final int z;

        private Cube(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            var cube = (Cube) obj;
            return cube.x == x && cube.y == y && cube.z == z;
        }

        @Override
        public int hashCode() {
            return String.format("%s,%s,%s", x, y, z).hashCode();
        }

        @Override
        public String toString() {
            return String.format("Cube[x=%s,y=%s,z=%s]", x, y, z);
        }
    }
}
