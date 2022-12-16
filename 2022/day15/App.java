import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 26);
        calculate(true, "mypuzzle.txt", 5716881);

        calculate(false, "example.txt", 56000011);
        calculate(false, "mypuzzle.txt", 10852583132904l);
    }    

    private static void calculate(Boolean assignment1, String dataset, long expected) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Parse sensors
        var sensors = new ArrayList<Sensor>();
        var regex = "Sensor at x=(-?[0-9]+), y=(-?[0-9]+): closest beacon is at x=(-?[0-9]+), y=(-?[0-9]+)";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(puzzle);
        while (matcher.find())
            sensors.add(new Sensor(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4))));

        // Calculate sensor detection ranges (= no beacon ranges)
        // First, join ranges for all sensors
        var sensorRanges = new HashMap<Integer, List<Range>>();
        for (var sensor : sensors) {
            var ranges = sensor.getDetectionRanges();
            for (var y : ranges.keySet()) {
                if (!sensorRanges.containsKey(y)) sensorRanges.put(y, new ArrayList<Range>());
                sensorRanges.get(y).add(ranges.get(y));
            }
        }
        // Then, merge overlapping ranges
        for (var y : sensorRanges.keySet()) {
            var ranges = sensorRanges.get(y);
            Collections.sort(ranges);
            var newRanges = new ArrayList<Range>();
            for (var i = 0; i < ranges.size(); i++) {
                var r1 = ranges.get(i);
                var x2 = r1.x2;
                for (var j = i+1; j < ranges.size(); j++) {
                    var r2 = ranges.get(j);
                    if (x2 < r2.x1) break; // no overlap
                    else { x2 = Math.max(x2,r2.x2); i++; } // (x2 >= r2.x1) overlap, so merge
                }
                newRanges.add(new Range(r1.x1, x2));
            }
            sensorRanges.put(y, newRanges);
        }

        // Assignment 1: count positions which cannot contain a beacon at target position
        var targetY = dataset.equals("example.txt") ? 10 : 2000000;
        var noBeaconCount = sensorRanges.get(targetY).stream().mapToInt(r -> r.x2-r.x1).reduce((a,b) -> a+b).getAsInt();
        
        // Assignment 2: calculate tuning frequency of distress signal
        long tuningFrequency;
        Point distressSignal = null;
        int minXY = 0, maxXY = 4000000;
        for (var y : sensorRanges.keySet().stream().filter(y -> y >= minXY && y <= maxXY).toList()) {
            var ranges = sensorRanges.get(y);
            for (var i = 0; i < ranges.size() - 1; i++) {
                var r1 = ranges.get(i);
                var r2 = ranges.get(i+1);
                if (r2.x1 > r1.x2) { distressSignal = new Point(r2.x1-1, y); break; }
            }
            if (distressSignal != null) break;
        }
        tuningFrequency = Long.valueOf(distressSignal.x) * maxXY + distressSignal.y;

        // Print results
        var actual = assignment1 ? noBeaconCount : tuningFrequency;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static final class Sensor {
        private final int x;
        private final int y;
        private final int bx;
        private final int by;

        private Sensor(int x, int y, int bx, int by) {
            this.x = x;
            this.y = y;
            this.bx = bx;
            this.by = by;
        }

        // Get sensor detection ranges (= positions which cannot contain beacons)
        // Returns a HashMap with Key=y and Range=x1,x2.
        private HashMap<Integer, Range> getDetectionRanges() {
            var md = Math.abs(x-bx) + Math.abs(y-by); // md = manhattan distance
            var ranges = new HashMap<Integer, Range>();
            for (var ry = this.y+md; ry >= this.y-md; ry--) {
                var maxDiff = md - Math.abs(ry - this.y);
                ranges.put(ry, new Range(this.x - maxDiff, this.x + maxDiff));
            }
            return ranges;
        }

        @Override
        public String toString() {
            return String.format("Sensor[x=%s,y=%s,bx=%s,by=%s]",x,y,bx,by);
        }
    }


    private static final class Range implements Comparable<Range> {
        private final int x1;
        private final int x2;

        private Range(int x1, int x2) {
            this.x1 = x1;
            this.x2 = x2;
        }

        @Override
        public String toString() {
            return String.format("Range[x1=%s,x2=%s]",x1,x2);
        }

        @Override
        public int compareTo(Range r) {
            if (x1 > r.x1) return 1;
            if (x1 < r.x1) return -1;
            if (x2 > r.x2) return 1;
            if (x2 < r.x2) return -1;
            return 0;
        }
    }

    private static final class Point {
        private final int x, y;
        private Point(int x, int y) { this.x = x; this.y = y; }
        @Override public String toString() { return String.format("Point[x=%s,y=%s]",x,y); }
    }
}
