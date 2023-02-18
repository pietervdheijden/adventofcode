import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 18);
        calculate(true, "mypuzzle.txt", 260);
        
        calculate(false, "example.txt", 54);
        calculate(false, "mypuzzle.txt", 747);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        final var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse blizzards
        final var blizzardList = new ArrayList<Blizzard>();
        final var lines = puzzle.lines().toList();
        final var maxY = lines.size()-1;
        final var maxX = lines.get(0).length()-1;
        for (var y = 0; y < lines.size(); y++) {
            final var line = lines.get(y);
            for (var x = 0; x < line.length(); x++) {
                final var c = line.charAt(x);
                if (c != '.' && c != '#') {
                    blizzardList.add(new Blizzard(c, new Position(x, y), maxX, maxY));
                }
            }
        }
        final var blizzards = new Blizzards(blizzardList);

        // Parse start and end positions
        final var startY = 0;
        final var startX = String.valueOf(lines.get(startY)).indexOf(".");
        final var endY = lines.size()-1;
        final var endX = String.valueOf(lines.get(endY)).lastIndexOf(".");
        final var startPosition = new Position(startX, startY);
        final var endPosition = new Position(endX, endY);
        
        // Assignment 1: Find fastest path from start position to end position
        var minMinute = findFastestPath(blizzards, 0, startPosition, endPosition, maxX, maxY);
        
        // Assignment 2: Find fastest path from end to start, and then from start to end
        if (!assignment1) {
            minMinute = findFastestPath(blizzards, minMinute, endPosition, startPosition, maxX, maxY);
            minMinute = findFastestPath(blizzards, minMinute, startPosition, endPosition, maxX, maxY);
        }

        // Print results
        final var actual = minMinute;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static int findFastestPath(Blizzards blizzards, int startMinute, Position startPosition, Position endPosition, int maxX, int maxY) {
        final var queue = new ArrayDeque<State>();
        queue.add(new State(startMinute, startPosition));
        var minMinute = Integer.MAX_VALUE;
        while (!queue.isEmpty()) {
            final var state = queue.removeFirst();
            
            // Queue new states
            for (var newState : state.getPotentialMoves()) {
                // Check if end position is reached
                if (newState.position.equals(endPosition)) {
                    minMinute = Math.min(newState.minute, minMinute);
                    continue;
                }
                // Check if new state is valid
                if (!queue.contains(newState) &&
                    newState.position.x > 0 &&
                    newState.position.x < maxX &&
                    newState.position.y >= 0 &&
                    (newState.position.y != 0 || newState.position.x == 1) && // allow y=0 in start position
                    newState.position.y <= maxY &&
                    (newState.position.y != maxY || newState.position.x == maxX-1) && // allow y=maxY in end position
                    !blizzards.isPositionTaken(newState.minute, newState.position) &&
                    state.minute < minMinute
                ) {
                    // Add valid state to queue
                    queue.add(newState);
                }
            }
        }
        return minMinute;
    }

    private static class Blizzards {
        private final Map<Integer, List<Blizzard>> blizzards = new HashMap<>();
        private final Map<Integer, Set<Position>> positions = new HashMap<>();

        private Blizzards(List<Blizzard> blizzardList) {
            this.blizzards.put(0, blizzardList);
            this.positions.put(0, blizzardList.stream().map(b -> b.position).collect(Collectors.toSet()));
        }

        private Set<Position> getPositions(int minute) {
            if (!blizzards.containsKey(minute)) {
                final var lastMinute = Collections.max(blizzards.keySet());
                for (var m = lastMinute + 1; m <= minute; m++) {
                    blizzards.put(m, blizzards.get(m-1).stream().map(b -> b.move()).toList());
                    positions.put(m, blizzards.get(m).stream().map(b -> b.position).collect(Collectors.toSet()));
                }
            }
            return positions.get(minute);
        }

        private boolean isPositionTaken(int minute, Position position) {
            return getPositions(minute).contains(position);
        }
    }

    private record Position (int x, int y) {
        private Position moveLeft() { 
            return new Position(x-1, y);
        }
        private Position moveRight() { 
            return new Position(x+1, y);
        }
        private Position moveUp() { 
            return new Position(x, y-1);
        }
        private Position moveDown() { 
            return new Position(x, y+1);
        }
        private Position moveWait() { 
            return new Position(x, y);
        }
    };

    private record Blizzard (char direction, Position position, int maxX, int maxY) {
        private Blizzard move() {
            final var x = position.x;
            final var y = position.y;
            final var blizzard = switch (direction) {
                case '>' -> new Blizzard(direction, new Position(x + 1 != maxX ? x + 1 : 1, y), maxX, maxY);
                case '<' -> new Blizzard(direction, new Position(x - 1 != 0 ? x - 1 : maxX - 1, y), maxX, maxY);
                case 'v' -> new Blizzard(direction, new Position(x, y + 1 != maxY ? y + 1 : 1), maxX, maxY);
                case '^' -> new Blizzard(direction, new Position(x, y - 1 != 0 ? y - 1 : maxY - 1), maxX, maxY);
                default -> throw new RuntimeException("Unsupported direction: " + direction);
            };
            return blizzard;
        }
    };

    private record State (int minute, Position position){
        private List<State> getPotentialMoves() {
            return List.of(
                new State(minute+1, position.moveRight()),
                new State(minute+1, position.moveDown()),
                new State(minute+1, position.moveLeft()),
                new State(minute+1, position.moveUp()),
                new State(minute+1, position.moveWait())
            );
        }
    }
}
