import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 13);
        calculate(true, "mypuzzle.txt", 5198);

        calculate(false, "example.txt", 140);
        calculate(false, "mypuzzle.txt", 22344);
    }
    
    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        var result = 0;
        var packets = new ArrayList<>(puzzle.lines().filter(l -> !l.isEmpty()).map(l -> new Packet(l)).toList());
        if (assignment1) { // assignment 1: sum indexes of correctly ordered pairs
            for (var i = 0; i < packets.size() / 2; i++) {
                var leftPacket = packets.get(i*2);
                var rightPacket = packets.get(i*2+1);
                if (rightPacket.compareTo(leftPacket) >= 0)
                    result += i+1; 
            }
        } else { // assignment 2: multiply indexes of divider packets
            var dividerPacket1 = new Packet("[[2]]");
            var dividerPacket2 = new Packet("[[6]]");
            packets.add(dividerPacket1);
            packets.add(dividerPacket2);
            Collections.sort(packets);
            result = (packets.indexOf(dividerPacket1)+1) * (packets.indexOf(dividerPacket2)+1);
        }

        // Print results
        var actual = result;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));

    }

    private static final class Packet implements Comparable<Packet> {
        public final String data;

        public Packet(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return String.format("Packet[value=%s]", data);
        }

        @Override
        public int compareTo(Packet p) {
            var lData = p.data; // left data
            var rData = data; // right data
            for (var i = 0; i < lData.length() && i < rData.length(); i++) {
                var l = lData.charAt(i);
                var l2 = (i < lData.length()-1 && isInt(lData.charAt(i+1))) ? lData.charAt(i+1) : Character.MIN_VALUE; // support two digit numbers
                var r = rData.charAt(i);
                var r2 = (i < rData.length()-1 && isInt(rData.charAt(i+1))) ? rData.charAt(i+1) : Character.MIN_VALUE; // support two digit numbers
                if (l == '[' && r == '[')
                    continue;
                if (l == '[' && isInt(r)) {
                    rData = addChar(rData, '[', i);
                    rData = addChar(rData, ']', i+2 + (r2 != Character.MIN_VALUE ? 1 : 0));
                    continue;
                }
                if (r == '[' && isInt(l)) {
                    lData = addChar(lData, '[', i);
                    lData = addChar(lData, ']', i+2 + (l2 != Character.MIN_VALUE ? 1 : 0));
                    continue;
                }
                if (isInt(l) && isInt(r)) {
                    var lInt = Integer.parseInt(Character.toString(l) + ((l2 != Character.MIN_VALUE) ? l2 : ""));
                    var rInt = Integer.parseInt(Character.toString(r) + ((r2 != Character.MIN_VALUE) ? r2 : ""));
                    if (lInt > rInt) return -1;
                    if (rInt > lInt) return 1;
                }
                if (l == ',' && r == ']') return -1;
                if (l == '[' && r == ']') return -1;
                if (l == ']' && r != ']') return 1;
                if (isInt(l) && r == ']') return -1;
            }
            return 0;
        }

        private boolean isInt(char c) {
            return "0123456789".indexOf(c) != -1;
        }

        // Source: https://www.baeldung.com/java-add-character-to-string
        private String addChar(String str, char ch, int position) {
            return str.substring(0, position) + ch + str.substring(position);
        }
    }
}
