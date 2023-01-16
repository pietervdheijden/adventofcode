import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        calculate(true, "example.txt", 33);
        calculate(true, "mypuzzle.txt", 2301);
        calculate(false, "example.txt", 3472);
        calculate(false, "mypuzzle.txt", 10336);
    }    

    // Solution is inspired by: https://github.com/encse/adventofcode/blob/master/2022/Day19/Solution.cs.
    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        final var puzzle = Files.readString(Paths.get("datasets/" + dataset));
       
        final var results = new ArrayList<Result>();
        final var pattern = Pattern.compile("Blueprint ([0-9]+): Each ore robot costs ([0-9]+) ore. Each clay robot costs ([0-9]+) ore. Each obsidian robot costs ([0-9]+) ore and ([0-9]+) clay. Each geode robot costs ([0-9]+) ore and ([0-9]+) obsidian.");
        final var blueprints = pattern.matcher(puzzle).results().toList();
        final var minutes = assignment1 ? 24 : 32;
        final var maxBlueprints = assignment1 ? blueprints.size() : Math.min(3, blueprints.size());
        for (var i = 0; i < maxBlueprints; i++) {
            final var blueprint = blueprints.get(i);
            final var blueprintId = Integer.parseInt(blueprint.group(1));
            final List<Robot> robots = List.of(
                new Robot(
                    new Resources(Integer.parseInt(blueprint.group(2)), 0, 0, 0),
                    new Resources(1, 0, 0, 0)),
                new Robot(
                    new Resources(Integer.parseInt(blueprint.group(3)), 0, 0, 0),
                    new Resources(0, 1, 0, 0)),
                new Robot(
                    new Resources(Integer.parseInt(blueprint.group(4)), Integer.parseInt(blueprint.group(5)), 0, 0),
                    new Resources(0, 0, 1, 0)),
                new Robot(
                    new Resources(Integer.parseInt(blueprint.group(6)), 0, Integer.parseInt(blueprint.group(7)), 0),
                    new Resources(0, 0, 0, 1))
            );
            final var maxResources = new Resources(
                robots.stream().mapToInt(r -> r.costs.ore).max().getAsInt(),
                robots.stream().mapToInt(r -> r.costs.clay).max().getAsInt(),
                robots.stream().mapToInt(r -> r.costs.obisidian).max().getAsInt(),
                Integer.MAX_VALUE // collect as many geodes as possible
            );
            var maxGeodes = -1;
            final var queue = new LinkedList<State>();
            queue.add(new State(new Resources(0,0,0,0), new Resources(1, 0, 0, 0), 0, Collections.emptyList()));
            while (!queue.isEmpty()) {
                final var state = queue.remove();

                // End state reached
                if (state.minute == minutes) {  
                    maxGeodes = Math.max(maxGeodes, state.resources.geode);
                    continue;
                }

                // Optimization: prune if upperbound < current max
                if (state.getUpperbound(minutes) <= maxGeodes) {
                    continue;
                }

                // Flow 1: build a robot
                var robotsBuyable = robots.stream().filter(r -> state.buyable(r)).toList();
                var robotsNeeded = robots.stream().filter(r -> state.needed(r, maxResources)).toList();
                for (var robot : robots
                    .stream()
                    .filter(r -> 
                        // Check if there are enough resources to buy the robot
                        robotsBuyable.contains(r)
                        // Optimization: prune if collector is not needed
                        && robotsNeeded.contains(r)
                        // Optimization: prune if robot could've been built in a previous minute
                        && !state.skipRobots.contains(r)
                    )
                    .toList()) {
                        // Optimization: add robot to beginning of queue
                        queue.addFirst(state.collect().build(robot));
                }

                // Flow 2: collect resources
                // Optimization: skip robots which could've been built in the current minute
                queue.add(state.collect().skip(robotsBuyable));
            }
            results.add(new Result(blueprintId, maxGeodes));
        }

        // Print results
        var actual = assignment1 ?
            results.stream().map(r -> r.blueprintId * r.maxGeodes).reduce((a,b) -> a + b).get()
            : results.stream().map(r -> r.maxGeodes).reduce((a,b) -> a * b).get();
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private final static class Result {
        private final int blueprintId;
        private final int maxGeodes;

        private Result(int blueprintId, int maxGeodes) {
            this.blueprintId = blueprintId;
            this.maxGeodes = maxGeodes;
        }

        @Override
        public String toString() {
            return String.format("Result[blueprintId=%s,maxGeodes=%s]", blueprintId, maxGeodes);
        }
    }

    private final static class Robot {
        private final Resources costs; 
        private final Resources collects;

        private Robot (Resources costs, Resources collects) {
            this.costs = costs;
            this.collects = collects;
        }

        @Override
        public String toString() {
            return String.format("Robot[costs=%s,collects=%s]", costs, collects);
        }
    }

    private final static class Resources {
        private final int ore;
        private final int clay;
        private final int obisidian;
        private final int geode;

        private Resources(int ore, int clay, int obisidian, int geode) {
            this.ore = ore;
            this.clay = clay;
            this.obisidian = obisidian;
            this.geode = geode;
        }

        private Resources subtract(Resources resources) {
            return new Resources(
                this.ore - resources.ore,
                this.clay - resources.clay,
                this.obisidian - resources.obisidian,
                this.geode - resources.geode);
        }

        private Resources add(Resources resources) {
            return new Resources(
                this.ore + resources.ore,
                this.clay + resources.clay,
                this.obisidian + resources.obisidian,
                this.geode + resources.geode);
        }

        @Override
        public String toString() {
            return String.format("Resources[ore=%s,clay=%s,obisidian=%s,geode=%s]", ore, clay, obisidian, geode);
        }
    }

    private final static class State {
        private final Resources resources;
        private final Resources collectors;
        private final int minute;
        private final List<Robot> skipRobots;

        private State(Resources resources, Resources collectors, int minute, List<Robot> skipRobots) {
            this.resources = resources;
            this.collectors = collectors;
            this.minute = minute;
            this.skipRobots = skipRobots;
        }

        private boolean buyable(Robot robot) {
            return robot.costs.ore <= resources.ore 
                && robot.costs.clay <= resources.clay
                && robot.costs.obisidian <= resources.obisidian 
                && robot.costs.geode <= resources.geode;
        }

        private boolean needed(Robot robot, Resources maxResources) {
            // Optimization: prune if there are sufficient collectors (collectors >= max resource)
            // This works since only 1 robot can be built per minute
            return (robot.collects.ore == 0 || collectors.ore < maxResources.ore)
                && (robot.collects.clay == 0 || collectors.clay < maxResources.clay)
                && (robot.collects.obisidian == 0 || collectors.obisidian < maxResources.obisidian)
                && (robot.collects.geode == 0 || collectors.geode < maxResources.geode);
        }

        private State collect() {
            return new State(resources.add(collectors), collectors, minute+1, Collections.emptyList());
        }

        private State skip(List<Robot> skipRobots) {
            return new State(resources, collectors, minute, skipRobots);
        }

        private State build(Robot robot) {
            return new State(
                resources.subtract(robot.costs),
                collectors.add(robot.collects),
                minute,
                Collections.emptyList());
        }

        private int getUpperbound(int totalMinutes) {
            final var remainingMinutes = totalMinutes - minute;
            return
                // Current geodes
                resources.geode 

                // Future geodes collected by current robots
                + collectors.geode * remainingMinutes 

                // Future geodes collected by future robots
                // Assume 1 new geode robot is built every minute (best case) -> geodes += n*(n-1)/2
                + ((remainingMinutes * (remainingMinutes - 1)) / 2);
        }

        @Override
        public String toString() {
            return String.format("State[resources=%s, collectors=%s, minute=%s, skipRobots=%s]", resources, collectors, minute, skipRobots);
        }
    }
}
