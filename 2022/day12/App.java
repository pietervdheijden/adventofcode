import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 31);
        calculate(true, "mypuzzle.txt", 462);

        calculate(false, "example.txt", 29);
        calculate(false, "mypuzzle.txt", 451);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));
        
        // Parse squares (=heightmap)
        var lines = puzzle.lines().toList();
        var squares = new Square[lines.size()][];
        for (var y = 0; y < lines.size(); y++) {
            var chars = lines.get(y).toCharArray();
            squares[y] = new Square[chars.length];
            for (var x = 0; x < chars.length; x++)
                squares[y][x] = new Square(x, y, chars[x]);
        }

        // Generate graph
        var graph = new HashMap<Square, ArrayList<Edge>>();
        for (var y = 0; y < squares.length; y++) {
            for (var x = 0; x < squares[y].length; x++) {
                // Get square
                var square = squares[y][x];
                // Get adjacent squares
                var adjacentSquares = new ArrayList<Square>();
                if (x > 0)
                    adjacentSquares.add(squares[y][x-1]);
                if (x < squares[y].length-1)
                    adjacentSquares.add(squares[y][x+1]);
                if (y > 0)
                    adjacentSquares.add(squares[y-1][x]);
                if (y < squares.length-1)
                    adjacentSquares.add(squares[y+1][x]);
                // Get edges
                var edges = new ArrayList<Edge>();
                for (var adjacentSquare : adjacentSquares) {
                    if ((Character.isLowerCase(adjacentSquare.elevation) && adjacentSquare.elevation <= square.elevation + 1)
                    ||  (adjacentSquare.elevation == 'a' && square.elevation == 'S')
                    ||  (adjacentSquare.elevation == 'S' && square.elevation == 'a')
                    ||  (adjacentSquare.elevation == 'E' && square.elevation == 'z')
                    ||  (adjacentSquare.elevation == 'E' && square.elevation == 'y') // Tricky! E has elevation z, so can be reached from z AND y.
                    )
                        edges.add(new Edge(adjacentSquare, 1));
                }
                // Add square to graph
                graph.put(square, edges);
            }
        }

        // Get start and end squares
        var startSquares = graph.keySet().stream().filter(s -> s.elevation == 'S' || (!assignment1 && s.elevation == 'a')).toList();
        var endSquare = graph.keySet().stream().filter(s -> s.elevation == 'E').findFirst().get();
        
        // For each start square, apply Dijkstra's algorithm to find the shortest path to end square
        // Source: https://www.freecodecamp.org/news/dijkstras-shortest-path-algorithm-visual-introduction/
        var minSteps = Integer.MAX_VALUE;
        for (var startSquare : startSquares) {
            var visitedNodes = new ArrayList<>(graph.keySet().stream().filter(s -> s == startSquare).toList());
            var unvisitedNodes = new ArrayList<>(graph.keySet().stream().filter(s -> s != startSquare).toList());
            var distances = graph.keySet().stream().collect(Collectors.toMap(entry -> entry, entry -> (entry == startSquare) ? 0 : Integer.MAX_VALUE));
            while (unvisitedNodes.size() != 0) {
                var minDistance = Integer.MAX_VALUE;
                Square minSquare = null;
                for (var visitedNode : visitedNodes) {
                    for (var edge : graph.get(visitedNode).stream().filter(edge -> unvisitedNodes.contains(edge.square)).toList()) {
                        var distance = Math.min(distances.get(edge.square), distances.get(visitedNode) + edge.weight);
                        distances.put(edge.square, distance);
                        if (distance < minDistance) {
                            minSquare = edge.square;
                            minDistance = distance;
                        }
                    }
                }
                if (minSquare == null) // the remaining squares are not reachable
                    break;
                visitedNodes.add(minSquare);
                unvisitedNodes.remove(minSquare);
                // performance optimization: remove visited nodes if all adjacent nodes are already visited
                visitedNodes.removeIf(visitedNode -> !graph.get(visitedNode).stream().anyMatch(edge -> unvisitedNodes.contains(edge.square)));
                // performance optimization: break when shortest path to end square has been found
                if (minSquare == endSquare)
                    break;
            }
            var steps = distances.get(endSquare);
            if (steps > 0)
                minSteps = Math.min(minSteps, steps);
        }
        
        // Print results
        var actual = minSteps;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static class Square {
        public final int x;
        public final int y;
        public final char elevation;

        public Square(int x, int y, char elevation) {
            this.x = x;
            this.y = y;
            this.elevation = elevation;
        }

        @Override public String toString() {
            return String.format("Square[x=%s,y=%s,elevation=%s]", x, y, elevation);
        }
    }

    private static class Edge {
        public final Square square;
        public final int weight;

        public Edge(Square square, int weight) {
            this.square = square;
            this.weight = weight;
        }
    }
}
