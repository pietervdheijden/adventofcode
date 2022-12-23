import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 1651);
        calculate(true, "mypuzzle.txt", 1820);

        calculate(false, "example.txt", 1707);
        calculate(false, "mypuzzle.txt", 2602);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));
        
        // Parse valves
        var valves = new ArrayList<Valve>();
        var matcher = Pattern.compile("Valve ([A-Z]+) has flow rate=([0-9]+); tunnels? leads? to valves? ([A-Z, ]+)").matcher(puzzle);
        while (matcher.find())
            valves.add(new Valve(matcher.group(1), Integer.parseInt(matcher.group(2)), matcher.group(3).split(", ")));

        // Calculate max pressure
        var maxPressure = assignment1 ?
            findMaxPressure(valves, List.of(new Path(List.of("AA"))), -1, 30) // assignment 1: use 1 path (you) and 30 minutes
            : findMaxPressure(valves, List.of(new Path(List.of("AA")), new Path(List.of("AA"))), -1, 26); //ass2: use 2 paths (you + elephant) and 26 minutes
        
        // Print results
        var actual = maxPressure;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    // The current algorithm brute forces all possible combinations (with some optimizations: cycles, )
    // However, it's much more efficient to use the BFS algorithm (Bread First Search)
    // For example, see: https://github.com/anthonywritescode/aoc2022/blob/main/day16/part1.py
    private static int findMaxPressure(List<Valve> valves, List<Path> paths, int currentMaxPressure, final int minutes) {
        var pressure = paths.stream().mapToInt(p -> p.pressure(valves, minutes)).sum();
        var pathSize = paths.get(0).moves.size();
        var openValves = paths.stream().map(p -> p.openValves()).flatMap(Collection::stream).toList();
        
        // Break recursion when path contains duplicate open valves or when the path contains a cycle
        if (new HashSet<String>(openValves).size() != openValves.size() // duplicate open value
        || paths.stream().anyMatch(p -> p.containsCycle())) // cycle
            return -1;
        
        // Break recursion when maxRemainingPressure <= currentMaxPressure
        var remainingValves = new ArrayList<Integer>(valves.stream().filter(v -> v.flowRate > 0 && !openValves.contains(v.name)).mapToInt(m -> m.flowRate).boxed().sorted(Comparator.reverseOrder()).toList());
        var maxRemainingPressure = pressure;
        for (var i = 0; i < remainingValves.size() && i < minutes - pathSize; i++)
            maxRemainingPressure += remainingValves.get(i) * (minutes - pathSize - i*2 + 1);
        if (maxRemainingPressure <= currentMaxPressure) return -1;

        // Return if time's up or if all valves with flowRate > 0 are open
        if (pathSize == minutes || openValves.size() + valves.stream().filter(v -> v.flowRate == 0).count() == valves.size())
            return pressure;

        // Time is not yet up nor are all valves already opened, therefore recursively try all permutations
        List<List<String>> options = new ArrayList<List<String>>();
        for (var i = 0 ; i < paths.size(); i++) {
            options.add(new ArrayList<String>());
            final int fi = i;
            var currentValve = valves.stream().filter(va -> va.name.equals(paths.get(fi).currentValve())).findFirst().get();
            if (currentValve.flowRate > 0 && !openValves.contains(currentValve.name)) options.get(i).add("+"); // open valve
            for (var tunnel : currentValve.tunnels) options.get(i).add(tunnel); // visit tunnels
        }
        for (var option : CartesianProductUtil.cartesianProduct(options)) {
            var newPaths = new ArrayList<Path>();
            for (var i = 0; i < paths.size(); i++) {
                newPaths.add(paths.get(i).clone());
                newPaths.get(i).add(option.get(i));
            }
            currentMaxPressure = Math.max(currentMaxPressure, findMaxPressure(valves, newPaths, currentMaxPressure, minutes));
        }
        return currentMaxPressure;
    }

    private static final class Valve {
        private final String name;
        private final int flowRate;
        private final String[] tunnels;

        private Valve(String name, int flowRate, String[] tunnels) {
            this.name = name;
            this.flowRate = flowRate;
            this.tunnels = tunnels;
        }

        @Override
        public String toString() {
            return String.format("Valve[name=%s,flowRate=%s,tunnels=%s]",name,flowRate,String.join(",",tunnels));
        }
    }

    private static final class Path implements Cloneable {
        private final List<String> moves;

        private Path(List<String> moves) {
            this.moves = new ArrayList<>(moves);
        }
        
        private void add(String move) {
            moves.add(move);
        }

        private List<String> openValves() {
            var openValves = new ArrayList<String>();
            for (var i = 0; i < moves.size() - 1; i++) {
                if (moves.get(i+1).equals("+")) openValves.add(moves.get(i));
            }
            return openValves;
        }

        private String currentValve() {
            return moves.stream().filter(m -> !m.equals("+")).reduce((first, second) -> second).get();
        }

        private boolean containsCycle() {
            var cv = moves.get(moves.size()-1);
            for (var i = moves.size()-2; i >= 0; i--) {
                if (moves.get(i).equals("+")) return false; // no cycle
                if (moves.get(i).equals(cv)) return true; // cycle
            }
            return false;
        }

        private int pressure(List<Valve> valves, int minutes) {
            var pressure = 0;
            for (var i = 0; i < moves.size(); i++) {
                final int fi = i;
                if (moves.get(i).equals("+")) { // todo
                    var valve = valves.stream().filter(v -> v.name.equals(moves.get(fi-1))).findFirst().get();
                    pressure += (minutes - i) * valve.flowRate;
                }
            }
            return pressure;
        }

        @Override
        public Path clone() {
            return new Path(new ArrayList<>(moves));
        }
        
        @Override
        public String toString() {
            return String.format("Path="+String.join(",", moves));
        }
    }

    // Source: https://gist.github.com/ThomasPr/8e038d5ebca97261940bf1dd13d3417d
    private static final class CartesianProductUtil {

        private CartesianProductUtil() { }
      
        /**
         * Compute the cartesian product for n lists.
         * The algorithm employs that A x B x C = (A x B) x C
         *
         * @param listsToJoin [a, b], [x, y], [1, 2]
         * @return [a, x, 1], [a, x, 2], [a, y, 1], [a, y, 2], [b, x, 1], [b, x, 2], [b, y, 1], [b, y, 2]
         */
        public static <T> List<List<T>> cartesianProduct(List<List<T>> listsToJoin) {
          if (listsToJoin.isEmpty()) {
            return new ArrayList<>();
          }
      
          listsToJoin = new ArrayList<>(listsToJoin);
          List<T> firstListToJoin = listsToJoin.remove(0);
          Stream<List<T>> startProduct = joinLists(new ArrayList<T>(), firstListToJoin);
      
          BinaryOperator<Stream<List<T>>> noOp = (a, b) -> null;
      
          return listsToJoin.stream() //
              .filter(Objects::nonNull) //
              .filter(list -> !list.isEmpty()) //
              .reduce(startProduct, CartesianProductUtil::joinToCartesianProduct, noOp) //
              .collect(toList());
        }
      
        /**
         * @param products [a, b], [x, y]
         * @param toJoin   [1, 2]
         * @return [a, b, 1], [a, b, 2], [x, y, 1], [x, y, 2]
         */
        private static <T> Stream<List<T>> joinToCartesianProduct(Stream<List<T>> products, List<T> toJoin) {
          return products.flatMap(product -> joinLists(product, toJoin));
        }
      
        /**
         * @param list   [a, b]
         * @param toJoin [1, 2]
         * @return [a, b, 1], [a, b, 2]
         */
        private static <T> Stream<List<T>> joinLists(List<T> list, List<T> toJoin) {
          return toJoin.stream().map(element -> appendElementToList(list, element));
        }
      
        /**
         * @param list    [a, b]
         * @param element 1
         * @return [a, b, 1]
         */
        private static <T> List<T> appendElementToList(List<T> list, T element) {
          int capacity = list.size() + 1;
          ArrayList<T> newList = new ArrayList<>(capacity);
          newList.addAll(list);
          newList.add(element);
          return unmodifiableList(newList);
        }
      }
}
