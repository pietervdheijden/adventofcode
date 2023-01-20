import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 3l);
        calculate(true, "mypuzzle.txt", 4_066l);
        calculate(false, "example.txt", 1_623_178_306l);
        calculate(false, "mypuzzle.txt", 6_704_537_992_933l);
    }    

    private static void calculate(Boolean assignment1, String dataset, long expected) throws IOException {
        // Parse numbers
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));
        var numbers = new ArrayList<Number>();
        for (var line : puzzle.lines().toList()) {
            numbers.add(new Number(Long.parseLong(line) * (assignment1 ? 1 : 811_589_153)));
        }

        // Mix numbers
        var mixedNumbers = new ArrayList<>(numbers);
        for (var i = 0; i < (assignment1 ? 1 : 10); i++) {
            for (var number : numbers) {
                final var index = mixedNumbers.indexOf(number);
                final var indexDiff = (int)(number.value % (long)(numbers.size()-1));
                
                // First, remove number
                mixedNumbers.remove(index);

                // Then, shift the number to the new position
                var newIndex = index + indexDiff;
                if (newIndex >= numbers.size()-1) {
                    newIndex += 1;
                    newIndex %= numbers.size();
                }
                if (newIndex <= 0) {
                    newIndex -= 1;
                    newIndex = numbers.size() + newIndex;
                }
                mixedNumbers.add(newIndex, number);
            }
        }

        // Sort mixed numbers, such that 0 is at the front
        final var number0 = numbers.stream().filter(n -> n.value == 0).findFirst().get();
        final var index0 = mixedNumbers.indexOf(number0);
        final var sortedMixedNumbers = new ArrayList<Number>();
        sortedMixedNumbers.addAll(mixedNumbers.subList(index0, mixedNumbers.size()));
        sortedMixedNumbers.addAll(mixedNumbers.subList(0, index0));

        // Calculate result
        var result = 
            sortedMixedNumbers.get(1000 % sortedMixedNumbers.size()).value
            + sortedMixedNumbers.get(2000 % sortedMixedNumbers.size()).value
            + sortedMixedNumbers.get(3000 % sortedMixedNumbers.size()).value;

        // Print results
        var actual = result;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    // Use enclosing class, since Longs with the same value re-use the same object
    private static class Number {
        private final Long value;

        private Number(Long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
