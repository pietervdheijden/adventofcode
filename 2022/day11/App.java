import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 10605);
        calculate(true, "mypuzzle.txt", 51075);
        calculate(false, "example.txt", 2713310158l);
        calculate(false, "mypuzzle.txt", 11741456163l);
    }    

    private static void calculate(Boolean assignment1, String dataset, long expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse monkeys
        var monkeys = new ArrayList<Monkey>();
        var regex = """
Monkey ([0-9]+):
  Starting items: (.+)
  Operation: new = (.+) ([\\*|\\+]) (.+)
  Test: divisible by ([0-9]+)
    If true: throw to monkey ([0-9])
    If false: throw to monkey ([0-9])""";
        var matcher = Pattern.compile(regex).matcher(puzzle);
        while (matcher.find()) {
            var id = Integer.parseInt(matcher.group(1));
            var items = Arrays.stream(matcher.group(2).split(", ")).mapToLong(Long::parseLong).boxed().toList();
            var operator = matcher.group(4).charAt(0);
            var operatorValue = matcher.group(5);
            var divisibleBy = Integer.parseInt(matcher.group(6));
            var throwToMonkeyWhenTrue = Integer.parseInt(matcher.group(7));
            var throwToMonkeyWhenFalse = Integer.parseInt(matcher.group(8));
            monkeys.add(new Monkey(id, items, operator, operatorValue, divisibleBy, throwToMonkeyWhenTrue, throwToMonkeyWhenFalse));
        }
        
        // Execute rounds
        var lcm = (int) lcm(monkeys.stream().mapToLong(m -> m.divisibleBy).toArray()); // Multiplying all divisibleBy numbers would yield the same result since they are all prime numbers
        for (var round = 1; round <= (assignment1 ? 20 : 10000); round++) {
            for (var monkey : monkeys) {
                // Inspect
                for (var i = 0; i < monkey.items.size(); i++) {
                    var newItem = monkey.items.get(i);
                    var operatorValue = (monkey.operatorValue.equals("old")) ? newItem : Long.parseLong(monkey.operatorValue);
                    if (monkey.operator == '*')
                        newItem *= operatorValue;
                    else
                        newItem += operatorValue;
                    if (assignment1) 
                        // Assignment 1: divide by 3
                        newItem /= 3l;
                    else 
                        // Assignment 2: mod by lcm (least common multiple)
                        // Theory: modulo congruence is preserved for any multiplication or addition operations
                        // Source: https://aoc.just2good.co.uk/2022/11#part-2
                        newItem %= lcm; 
                    monkey.items.set(i, newItem);
                    monkey.inspectCount++;
                }
                // Throw
                for (var item : monkey.items)
                    monkeys.get(item%monkey.divisibleBy == 0 ? monkey.throwToMonkeyWhenTrue : monkey.throwToMonkeyWhenFalse).items.add(item);
                monkey.items.clear();
            }
        }
        var monkeyBusiness = monkeys.stream().map(m -> m.inspectCount).sorted(Collections.reverseOrder()).limit(2).reduce((a,b) -> (a*b)).get();
        
        // Print results
        var actual = monkeyBusiness;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static class Monkey {
        public final int id;
        public final List<Long> items;
        public final char operator;
        public final String operatorValue;
        public final int divisibleBy;
        public final int throwToMonkeyWhenTrue;
        public final int throwToMonkeyWhenFalse;
        public Long inspectCount;

        public Monkey(int id, List<Long> items, char operator, String operatorValue, int divisibleBy, int throwToMonkeyWhenTrue, int throwToMonkeyWhenFalse) {
            this.id = id;
            this.items = new ArrayList<>(items);
            this.operator = operator;
            this.operatorValue = operatorValue;
            this.divisibleBy = divisibleBy;
            this.throwToMonkeyWhenTrue = throwToMonkeyWhenTrue;
            this.throwToMonkeyWhenFalse = throwToMonkeyWhenFalse;
            this.inspectCount = 0l;
        }

        @Override public String toString() {
            return String.format("Monkey[id=%s]", id);
        }
    }

    // Helper functions to calculate lcm (Least Common Multiplier)
    // Source: https://stackoverflow.com/a/53549432/3737152
    private static long gcd(long a, long b)
    {
        while (b > 0)
        {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }
    private static long lcm(long a, long b)
    {
        return a * (b / gcd(a, b));
    }
    private static long lcm(long[] input)
    {
        long result = input[0];
        for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }
}
