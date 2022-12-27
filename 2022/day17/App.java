import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 3068);
        calculate(true, "mypuzzle.txt", 3071);
        
        calculate(false, "example.txt", 1514285714288l);
        calculate(false, "mypuzzle.txt", 1523615160362L);
    }    

    private static void calculate(Boolean assignment1, String dataset, long expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // initialize
        var height = 0L;
        var cache = new HashMap<Integer, CacheItem>();
        var rocks = new ArrayList<ArrayList<char[]>>();
        for (var i = 0; i < 5; i++) rocks.add(new ArrayList<>());
        rocks.get(0).add("..@@@@.".toCharArray());
        rocks.get(1).add("...@...".toCharArray());
        rocks.get(1).add("..@@@..".toCharArray());
        rocks.get(1).add("...@...".toCharArray());
        rocks.get(2).add("....@..".toCharArray());
        rocks.get(2).add("....@..".toCharArray());
        rocks.get(2).add("..@@@..".toCharArray());
        rocks.get(3).add("..@....".toCharArray());
        rocks.get(3).add("..@....".toCharArray());
        rocks.get(3).add("..@....".toCharArray());
        rocks.get(3).add("..@....".toCharArray());
        rocks.get(4).add("..@@...".toCharArray());
        rocks.get(4).add("..@@...".toCharArray());
        var chamber = new ArrayList<char[]>();
        var instructions = puzzle.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        var instructionIndex = 0L;
        
        // add rocks to chamber
        var totalRocks = (assignment1 ? 2022 : 1_000_000_000_000l);
        for (var rockIndex = 0L; rockIndex < totalRocks; rockIndex++) {
            // add 3 rows
            for (var row = 0; row < 3; row++)
                chamber.add(".......".toCharArray());

            // add rock
            var rock = rocks.get((int)(rockIndex % rocks.size()));
            for (var row = rock.size()-1; row >= 0; row--)
                chamber.add(rock.get(row).clone());

            // move until rock is stuck
            var stuck = false;
            while (!stuck) {
                var instruction = instructions.get((int)(instructionIndex % instructions.size()));
                instructionIndex++;

                // move rock left or right (based on instruction)
                if (!chamber.stream().anyMatch(row ->
                    (instruction == '>' && (row[row.length-1] == '@' || String.valueOf(row).contains("@#"))
                ||  (instruction == '<' && (row[0] == '@' || String.valueOf(row).contains("#@")))))) {
                    for (var rowIndex = chamber.size() - 1; rowIndex >= 0; rowIndex--) {
                        var row = chamber.get(rowIndex);
                        if (instruction == '>') {
                            for (var columnIndex = row.length - 1; columnIndex > 0; columnIndex--) {
                                if (row[columnIndex-1] == '@') {
                                    row[columnIndex] = row[columnIndex-1];
                                    row[columnIndex-1] = '.';
                                }
                            }
                        } else { // instruction == '<'
                            for (var columnIndex = 0; columnIndex < row.length - 1; columnIndex++) {
                                if (row[columnIndex+1] == '@') {
                                    row[columnIndex] = row[columnIndex+1];
                                    row[columnIndex+1] = '.';
                                }
                            }
                        }
                    }
                }
                
                // move rock down if not stuck
                for (var rowIndex = 0; rowIndex < chamber.size() && !stuck; rowIndex++) {
                    for (var columnIndex = 0; columnIndex < 7 && !stuck; columnIndex++) {
                        if (chamber.get(rowIndex)[columnIndex] == '@' && (rowIndex == 0 || chamber.get(rowIndex-1)[columnIndex] == '#')) {
                            stuck = true;
                        }
                    }
                }
                for (var rowIndex = 0; rowIndex < chamber.size() && !stuck; rowIndex++) {
                    for (var columnIndex = 0; columnIndex < 7; columnIndex++) {
                        if (chamber.get(rowIndex)[columnIndex] == '@') {
                            chamber.get(rowIndex-1)[columnIndex] = chamber.get(rowIndex)[columnIndex];
                            chamber.get(rowIndex)[columnIndex] = '.';
                        }
                    }
                }
            }

            // update chamber and height
            height += Math.max(0,
                chamber.indexOf(chamber.stream().filter(row -> String.valueOf(row).contains("@")).reduce((first,second)->second).get())
                - chamber.indexOf(chamber.stream().filter(row -> String.valueOf(row).contains("#")).reduce((first,second)->second).orElse(null)));
            chamber = new ArrayList<>(chamber.stream()
                .map(row -> String.valueOf(row).replace("@","#")) // stop falling rock
                .filter(row -> row.contains("#")) // remove empty rows
                .map(row -> row.toCharArray())
                .skip(Math.max(chamber.size() - 50,0)) // only keep the last 50 rows (performance optimization)
                .toList());

            // use cache on chamber, rockIdxMod and instructionIdxMod to detect cycles
            // this performance optimization is required for part 2
            var cacheKey = getCacheKey(chamber, (int) (rockIndex % rocks.size()), (int)(instructionIndex % instructions.size()));
            if (cache.containsKey(cacheKey)) {
                // cycle found -> fast forward to the last repetition
                var cacheItem = cache.get(cacheKey);
                var rockDiff = rockIndex - cacheItem.rocks; // =cycleLength
                var heightDiff = height - cacheItem.height; // =cycleHeight
                var cycles = (totalRocks - rockIndex) / rockDiff;
                rockIndex += rockDiff * cycles;
                height += heightDiff * cycles;
                cache.clear();
            } else {
                cache.put(cacheKey, new CacheItem(rockIndex, height));
            }
        }
        
        // Print results
        var actual = height;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static int getCacheKey(ArrayList<char[]> chamber, int rockIndex, int instructionIndex) {
        var chamberString = "";
        for (var i = chamber.size()-1; i >= chamber.size()-20 && i >= 0; i--)
            chamberString += String.valueOf(chamber.get(i));

        var hashCode = 0;
        hashCode = chamberString.hashCode();
        hashCode = 31 * hashCode + rockIndex;
        hashCode = 31 * hashCode + instructionIndex;
        return hashCode;
    }

    private static class CacheItem {
        private final long rocks;
        private final long height;

        private CacheItem(long rocks, long height) {
            this.rocks = rocks;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.format("CacheItem[rocks=%s,height=%s]", rocks, height);
        }
    }
}
