import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 152l);
        calculate(true, "mypuzzle.txt", 31_017_034_894_002l);
        calculate(false, "example.txt", 301);
        calculate(false, "mypuzzle.txt", 3_555_057_453_229l);
    }    

    private static void calculate(Boolean assignment1, String dataset, long expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse monkeys
        var monkeys = new HashMap<String, Monkey>();
        for (var m : Pattern.compile("([a-z]+): ([a-z]+) (\\+|-|\\*|\\/) ([a-z]+)").matcher(puzzle).results().toList()) {
            monkeys.put(m.group(1), new Monkey(m.group(1), m.group(2), m.group(3), m.group(4)));
        }
        for (var m : Pattern.compile("([a-z]+): ([0-9]+)").matcher(puzzle).results().toList()) {
            monkeys.put(m.group(1), new Monkey(m.group(1), Long.parseLong(m.group(2))));
        }
        if (!assignment1) { // Assignment 2: overwrite root monkey and humn monkey
            final var rootMonkey = monkeys.get("root");
            final var humnMonkey = monkeys.get("humn");
            monkeys.put("root", new Monkey(rootMonkey.name, rootMonkey.monkey1, "=", rootMonkey.monkey2));
            monkeys.put("humn", new Monkey(humnMonkey.name, null));
        }

        // Process monkeys until all monkeys have a value
        while (monkeys.values().stream().anyMatch(m -> m.value == null)) {
            for (var m : monkeys.values()) {
                final var m1 = monkeys.get(m.monkey1);
                final var m2 = monkeys.get(m.monkey2);
                if (m1 == null || m2 == null) {
                    continue;
                }
                if (m.value == null && m.operation == "=" && m1.value != null) {
                    m.value = m1.value;
                    m2.value = m1.value;
                }
                if (m.value == null && m.operation == "=" && m2.value != null) {
                    m.value = m2.value;
                    m1.value = m2.value;
                }
                if (m.value == null && m1.value != null && m2.value != null) {
                    m.value = switch(m.operation) {
                        case "+" -> m1.value + m2.value;
                        case "-" -> m1.value - m2.value;
                        case "*" -> m1.value * m2.value;
                        case "/" -> m1.value / m2.value;
                        default -> m.value;
                    };
                }
                if (m.value != null && m1.value == null && m2.value != null) {
                    m1.value = switch(m.operation) {
                        case "+" -> m.value - m2.value;
                        case "-" -> m.value + m2.value;
                        case "*" -> m.value / m2.value;
                        case "/" -> m.value * m2.value;
                        default -> m1.value;
                    };
                }
                if (m.value != null && m1.value != null && m2.value == null) {
                    m2.value = switch(m.operation) {
                        case "+" -> m.value - m1.value;
                        case "-" -> m1.value - m.value;
                        case "*" -> m.value / m1.value;
                        case "/" -> m1.value / m.value;
                        default -> m2.value;
                    };
                }
            }
        }


        // Print results
        var actual = monkeys.get(assignment1 ? "root" : "humn").value;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static class Monkey {
        private final String name;
        private Long value;
        private final String monkey1;
        private final String operation;
        private final String monkey2;

        private Monkey(String name, Long value) {
            this.name = name;
            this.value = value;
            this.monkey1 = null;
            this.operation = null;
            this.monkey2 = null;
        }

        private Monkey(String name, String monkey1, String operation, String monkey2) {
            this.name = name;
            this.value = null;
            this.monkey1 = monkey1;
            this.operation = operation;
            this.monkey2 = monkey2;
        }

        @Override
        public String toString() {
            return String.format("Monkey[%s: %s=%s%s%s]", name, value, monkey1, operation, monkey2);
        }
    }
}
