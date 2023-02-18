import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", "2=-1=0");
        calculate(true, "mypuzzle.txt", "2-2--02=1---1200=0-1");
    }    

    private static void calculate(Boolean assignment1, String dataset, String expected) throws IOException {
        final var puzzle = Files.readString(Paths.get("datasets/" + dataset));
        final var sum = puzzle.lines().mapToLong(l -> SNAFUToDecimal(l)).sum();
        final var SNAFU = decimalToSNAFU(sum);

        // Print results
        var actual = SNAFU;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected.equals(actual) ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static long SNAFUToDecimal(String SNAFU) {
        var sum = 0l;
        var power = 1l;
        final var chars = SNAFU.toCharArray();
        for (var i = chars.length-1; i >= 0; i--) {
            final var c = chars[i];
            final var value = power * switch(c) {
                case '2' -> 2l;
                case '1' -> 1l;
                case '0' -> 0l;
                case '-' -> -1l;
                case '=' -> -2l;
                default -> throw new RuntimeException("Unsupported character: " + c);
            };
            sum += value;
            power *= 5l;
        }
        return sum;
    }

    private static String decimalToSNAFU(long decimal) {
        // Determine maxPower and powerSum
        final var powerSum = new HashMap<Long, Long>();
        powerSum.put(0l, 0l);
        var maxPower = 1l;
        while (true) {
            powerSum.put(maxPower, maxPower*2l + powerSum.get(maxPower/5l));
            if (powerSum.get(maxPower) >= decimal) {
                break;
            }
            maxPower *= 5l;
        }
        
        // Convert decimal to SNAFU
        var SNAFU = "";
        var remainingDecimal = decimal;
        for (var power = maxPower; power >= 1l; power /= 5l) {
            // Add 0
            if (Math.abs(remainingDecimal) <= powerSum.get(power / 5l) || remainingDecimal == 0l) {
                SNAFU += "0";
                continue;
            }

            // Add 1 or 2
            if (remainingDecimal > 0) {
                if (power * 1 + powerSum.get(power/5l) >= remainingDecimal) {
                    SNAFU += "1";
                    remainingDecimal -= power * 1l;
                } else {
                    SNAFU += "2";
                    remainingDecimal -= power * 2l;
                }
                continue;
            }

            // Add - or =
            if (remainingDecimal < 0) {
                if (power * -1 - powerSum.get(power/5l) <= remainingDecimal) {
                    SNAFU += "-";
                    remainingDecimal += power * 1l;
                } else {
                    SNAFU += "=";
                    remainingDecimal += power * 2l;
                }
                continue;
            }
        }
        return SNAFU;
    }
}
