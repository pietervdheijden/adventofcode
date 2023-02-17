import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 110);
        calculate(true, "mypuzzle.txt", 3800);
        calculate(false, "example.txt", 20);
        calculate(false, "mypuzzle.txt", 916);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse elves
        final var elves = new HashSet<Elf>();
        final var lines = puzzle.lines().toList();
        for (var y = 0; y < lines.size(); y++) {
            final var line = lines.get(y);
            for (var x = 0; x < line.length(); x++) {
                if (line.charAt(x) == '#') {
                    elves.add(new Elf(x, y));
                }
            }
        }

        // Execute rounds
        final var moves = new ArrayList<Character>();
        moves.add('N');
        moves.add('S');
        moves.add('W');
        moves.add('E');
        var round = 0;
        while (true) {
            // Increment round
            round++;
                
            // First half: propose moves
            var proposedMoves = new ArrayList<ProposedMove>();
            for (var elf : elves) {
                final var elvesInNorthPosition = 
                    elves.contains(new Elf(elf.x-1, elf.y-1))
                    || elves.contains(new Elf(elf.x, elf.y-1))
                    || elves.contains(new Elf(elf.x+1, elf.y-1));
                final var elvesInSouthPosition = 
                    elves.contains(new Elf(elf.x-1, elf.y+1))
                    || elves.contains(new Elf(elf.x, elf.y+1))
                    || elves.contains(new Elf(elf.x+1, elf.y+1));
                final var elvesInWestPosition = 
                    elves.contains(new Elf(elf.x-1, elf.y-1))
                    || elves.contains(new Elf(elf.x-1, elf.y))
                    || elves.contains(new Elf(elf.x-1, elf.y+1));
                final var elvesInEastPosition = 
                    elves.contains(new Elf(elf.x+1, elf.y-1))
                    || elves.contains(new Elf(elf.x+1, elf.y))
                    || elves.contains(new Elf(elf.x+1, elf.y+1));
                
                if (!elvesInNorthPosition && !elvesInSouthPosition && !elvesInWestPosition && !elvesInEastPosition) {
                    // Stop moving
                    continue;
                }

                for (var move : moves) {
                    if (move == 'N' && !elvesInNorthPosition) {
                        proposedMoves.add(new ProposedMove(elf, new Elf(elf.x, elf.y-1)));
                        break;
                    }
                    if (move == 'S' && !elvesInSouthPosition) {
                        proposedMoves.add(new ProposedMove(elf, new Elf(elf.x, elf.y+1)));
                        break;
                    }
                    if (move == 'W' && !elvesInWestPosition) {
                        proposedMoves.add(new ProposedMove(elf, new Elf(elf.x-1, elf.y)));
                        break;
                    }
                    if (move == 'E' && !elvesInEastPosition) {
                        proposedMoves.add(new ProposedMove(elf, new Elf(elf.x+1, elf.y)));
                        break;
                    }
                }
            }
            Collections.rotate(moves, -1);

            // Second half: move
            final var duplicateNewElves = proposedMoves
                .stream()
                .map(p -> p.newElf)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != 1)
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
            var moved = false;
            for (var proposedMove : proposedMoves) {
                if (!duplicateNewElves.contains(proposedMove.newElf)) {
                    elves.remove(proposedMove.oldElf);
                    elves.add(proposedMove.newElf);
                    moved = true;
                }
            }

            // Check if done
            if (assignment1 && round == 10) {
                break;
            }
            if (!assignment1 && !moved) {
                break;
            }
        }

        // Assignment 1: calculate empty ground tiles
        final var minX = elves.stream().mapToInt(e -> e.x).min().getAsInt();
        final var maxX = elves.stream().mapToInt(e -> e.x).max().getAsInt();
        final var minY = elves.stream().mapToInt(e -> e.y).min().getAsInt();
        final var maxY = elves.stream().mapToInt(e -> e.y).max().getAsInt();
        final var emptyGroundTiles = (maxX - minX + 1) * (maxY - minY + 1) - elves.size();

        // Print results
        var actual = assignment1 ? emptyGroundTiles : round;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static class ProposedMove {
        private final Elf oldElf;
        private final Elf newElf;
        
        private ProposedMove(Elf oldElf, Elf newElf) {
            this.oldElf = oldElf;
            this.newElf = newElf;
        }
    }

    private static class Elf {
        private final int x;
        private final int y;

        private Elf(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            var otherElf = (Elf) o;
            return this.x == otherElf.x && this.y == otherElf.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return String.format("Elf[x=%s,y=%s]", this.x, this.y);
        }
    }
}
