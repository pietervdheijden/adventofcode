import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class App {
    public static void main(String args[]) throws IOException {
        calculate2(true, "example.txt", 6032);
        calculate2(true, "mypuzzle.txt", 20494);
        calculate2(false, "example.txt", 5031);
        calculate2(false, "mypuzzle.txt", 55343);
    }

    private static void calculate2(Boolean assignment1, String dataset, int expected) throws IOException {
        final var puzzle = Files.readString(Paths.get("datasets/" + dataset)).split("\n\n");

        // Set cube faces
        var cubeFaces = List.of(
            new CubeFace(Side.FRONT, Set.of(new Coordinate(0, 0, 0), new Coordinate(1, 0, 0), new Coordinate(0, 1, 0), new Coordinate(1, 1, 0))),
            new CubeFace(Side.LEFT, Set.of(new Coordinate(0, 0, 1), new Coordinate(0, 0, 0), new Coordinate(0, 1, 1), new Coordinate(0, 1, 0))),
            new CubeFace(Side.RIGHT, Set.of(new Coordinate(1, 0, 0), new Coordinate(1, 0, 1), new Coordinate(1, 1, 0), new Coordinate(1, 1, 1))),
            new CubeFace(Side.BACK, Set.of(new Coordinate(1, 0, 1), new Coordinate(0, 0, 1), new Coordinate(0, 1, 1), new Coordinate(1, 1, 1))),
            new CubeFace(Side.TOP, Set.of(new Coordinate(0, 1, 0), new Coordinate(1, 1, 0), new Coordinate(0, 1, 1), new Coordinate(1, 1, 1))),
            new CubeFace(Side.BOTTOM, Set.of(new Coordinate(0, 0, 0), new Coordinate(1, 0, 0), new Coordinate(0, 0, 1), new Coordinate(1, 0, 1)))
        );

        // Find first square
        var cubeMap = Arrays.stream(puzzle[0].split("\n")).map(l -> l.toCharArray()).toList();
        final var squareSize = dataset.equals("example.txt") ? 4 : 50;
        var line0String = new String(cubeMap.get(0));
        final var firstX = Math.min(line0String.indexOf("."), line0String.indexOf("#"));
        final var firstSquare = new Square(new Coordinate(0, 0, 0), new Coordinate(1, 0, 0), new Coordinate(0, 1, 0), new Coordinate(1, 1, 0), firstX, 0);

        // Get all squares
        var squares = new ArrayList<SquareMap>();
        var queue = new ArrayDeque<Square>();
        queue.add(firstSquare);
        while (!queue.isEmpty()) {
            var square = queue.pop();
            if (square.startX < 0 || square.startY < 0 || square.startY > cubeMap.size() - squareSize || square.startX > cubeMap.get(square.startY).length - squareSize) {
                // Square falls out of map
                continue;
            }
            if (cubeMap.get(square.startY)[square.startX] == ' ') {
                // Not a square
                continue;
            }

            // Get side
            final var side = cubeFaces.stream().filter(cf -> cf.coordinates.equals(Set.of(square.leftBottom, square.rightBottom, square.leftTop, square.rightTop))).findFirst().get().side;
            if (squares.stream().anyMatch(s -> s.side == side)) {
                // Square is already processed
                continue;
            }

            // Get square map
            final char[][] map = new char[squareSize][squareSize];
            for (var y = 0; y < squareSize; y++) {
                for (var x = 0; x < squareSize; x++) {
                    map[y][x] = cubeMap.get(y + square.startY)[x + square.startX];
                }
            }

            // Add square to squares
            squares.add(new SquareMap(side, square, map));

            // Add neighbor squares to queue
            queue.add(getNeighbor(square, side, Direction.LEFT, squareSize));
            queue.add(getNeighbor(square, side, Direction.RIGHT, squareSize));
            queue.add(getNeighbor(square, side, Direction.UP, squareSize));
            queue.add(getNeighbor(square, side, Direction.DOWN, squareSize));
        }

        // Parse instructions
        final var instructions = Pattern.compile("([0-9]+|[RL])").matcher(puzzle[1]).results().map(i -> i.group(1)).toList();

        // Set start position
        var square = squares.stream().filter(s -> s.side == Side.FRONT).findFirst().get();
        var row = 0;
        var column = String.valueOf(square.map[0]).indexOf(".");
        var direction = Direction.RIGHT;

        // Execute instructions
        for (var instruction : instructions) {
            if (instruction.charAt(0) == 'R') {
                direction = switch (direction) {
                    case RIGHT -> Direction.DOWN;
                    case DOWN -> Direction.LEFT;
                    case LEFT -> Direction.UP;
                    case UP -> Direction.RIGHT;
                };
            } else if (instruction.charAt(0) == 'L') {
                direction = switch (direction) {
                    case RIGHT -> Direction.UP;
                    case UP -> Direction.LEFT;
                    case LEFT -> Direction.DOWN;
                    case DOWN -> Direction.RIGHT;
                };
            } else {
                final var tilesToMove = Integer.parseInt(instruction);
                for (var i = 0; i < tilesToMove; i++) {

                    // Normal steps
                    if (direction == Direction.RIGHT && column < squareSize-1 && square.map[row][column+1] == '.') { column++; continue; }
                    if (direction == Direction.RIGHT && column < squareSize-1 && square.map[row][column+1] == '#') { break; }
                    if (direction == Direction.LEFT && column > 0 && square.map[row][column-1] == '.') { column--; continue; }
                    if (direction == Direction.LEFT && column > 0 && square.map[row][column-1] == '#') { break; }
                    if (direction == Direction.DOWN && row < squareSize-1 && square.map[row+1][column] == '.') { row++; continue; }
                    if (direction == Direction.DOWN && row < squareSize-1 && square.map[row+1][column] == '#') { break; }
                    if (direction == Direction.UP && row > 0 && square.map[row-1][column] == '.') { row--; continue; }
                    if (direction == Direction.UP && row > 0 && square.map[row-1][column] == '#') { break; }

                    // Flow to another square
                    if (assignment1) { // assignment 1: map
                        final var squareMaps = new SquareMaps(squares);
                        final var newSquare = switch(direction) {
                            case DOWN -> squareMaps.findByX(square.square.startX, square.square.startY + squareSize, squareSize * squareSize, squareSize)
                                .orElse(squareMaps.findByX(square.square.startX, 0, square.square.startY, squareSize).get());
                            case UP -> squareMaps.findByX(square.square.startX, square.square.startY - squareSize, 0, squareSize)
                                .orElse(squareMaps.findByX(square.square.startX, squareSize * squareSize, square.square.startY, squareSize).get());
                            case RIGHT -> squareMaps.findByY(square.square.startY, square.square.startX + squareSize, squareSize * squareSize, squareSize)
                                .orElse(squareMaps.findByY(square.square.startY, 0, square.square.startX, squareSize).get());
                            case LEFT -> squareMaps.findByY(square.square.startY, square.square.startX - squareSize, 0, squareSize)
                                .orElse(squareMaps.findByY(square.square.startY, squareSize * squareSize, square.square.startX, squareSize).get());
                        };
                        final var newRow = switch(direction) {
                            case DOWN -> 0;
                            case UP -> squareSize - 1;
                            case RIGHT -> row;
                            case LEFT -> row;
                        };
                        final var newColumn = switch(direction) {
                            case DOWN -> column;
                            case UP -> column;
                            case RIGHT -> 0;
                            case LEFT -> squareSize - 1;
                        };
                        if (newSquare.map[newRow][newColumn] == '.') {
                            square = newSquare;
                            row = newRow;
                            column = newColumn;
                            continue;
                        } else {
                            break;
                        }
                    } else { // assignment 2: cube
                        final var fsquare = square;
                        final var edge = fsquare.square.getDirectionCoordinates(direction);
                        final var newSquare = squares.stream().filter(s -> s.square.contains(edge) && s != fsquare).findFirst().get();
                        final var newDirection = newSquare.square.getDirection(edge);
                        final var newRow = switch(newDirection) {
                            case DOWN -> 0;
                            case UP -> squareSize - 1;
                            case RIGHT -> switch(direction) {
                                case RIGHT -> row;
                                case LEFT -> squareSize - row - 1;
                                case UP -> column;
                                case DOWN -> squareSize - column - 1;
                            };
                            case LEFT -> switch(direction) {
                                case RIGHT -> squareSize - row - 1;
                                case LEFT -> row;
                                case UP -> squareSize - column - 1;
                                case DOWN -> column;
                            };
                        };
                        final var newColumn = switch(newDirection) {
                            case RIGHT -> 0;
                            case LEFT -> squareSize - 1;
                            case DOWN -> switch(direction) {
                                case RIGHT -> squareSize - row - 1;
                                case LEFT -> row;
                                case UP -> squareSize - column - 1;
                                case DOWN -> column;
                            };
                            case UP -> switch(direction) {
                                case RIGHT -> row;
                                case LEFT -> squareSize - row - 1;
                                case UP -> column;
                                case DOWN -> squareSize - column - 1;
                            };
                        };
                        if (newSquare.map[newRow][newColumn] == '.') {
                            square = newSquare;
                            direction = newDirection;
                            row = newRow;
                            column = newColumn;
                            continue;
                        } else {
                            break;
                        }           
                    }   
                }
            }
        }

        // Calculate password
        final var password = 1000 * (square.square.startY + row+1) + 4 * (square.square.startX + column+1) + switch(direction) {
            case RIGHT -> 0;
            case DOWN -> 1;
            case LEFT -> 2;
            case UP -> 3;
        };

        // Print results
        final var actual = password;
        System.out.println(String.format("Assignment %s - dataset=%s [%s]\t: expected=%s, actual=%s", 
            assignment1 ? "1" : "2",
            dataset,
            expected == actual ? "PASSED" : "FAILED",
            expected,
            actual));
    }

    private static Square getNeighbor(Square square, Side side, Direction direction, int squareSize) {
        final var leftBottom = square.leftBottom;
        final var rightBottom = square.rightBottom;
        final var leftTop = square.leftTop;
        final var rightTop = square.rightTop;
        final var startX = square.startX;
        final var startY = square.startY;
        
        return switch(direction) {
            case DOWN -> new Square(rotate(side, leftBottom), rotate(side, rightBottom), leftBottom, rightBottom, startX, startY + squareSize); 
            case UP -> new Square(leftTop, rightTop, rotate(side, leftTop), rotate(side, rightTop), startX, startY - squareSize); 
            case LEFT -> new Square(rotate(side, leftBottom), leftBottom, rotate(side, leftTop), leftTop, startX - squareSize, startY); 
            case RIGHT -> new Square(rightBottom, rotate(side, rightBottom), rightTop, rotate(side, rightTop), startX + squareSize, startY); 
        };
    }

    private static Coordinate rotate(Side side, Coordinate coordinate) {
        final var x = coordinate.x;
        final var y = coordinate.y;
        final var z = coordinate.z;
        return switch (side) {
            case FRONT -> new Coordinate(x, y, 1);
            case BACK -> new Coordinate(x, y, 0);
            case BOTTOM -> new Coordinate(x, 1, z);
            case TOP -> new Coordinate(x, 0, z);
            case LEFT -> new Coordinate(1, y, z);
            case RIGHT -> new Coordinate(0, y, z);
        };
    }

    private static final class SquareMaps {
        private final List<SquareMap> squareMaps;
        private SquareMaps(List<SquareMap> squareMaps) {
            this.squareMaps = squareMaps;
        } 

        private Optional<SquareMap> findByX(int startX, int fromStartY, int toStartY, int squareSize) {
            if (fromStartY < 0 || toStartY < 0) return Optional.empty();
            Optional<SquareMap> newSquare = Optional.empty();
            if (fromStartY < toStartY) {
                for (var y = fromStartY; y <= toStartY && newSquare.isEmpty(); y += squareSize) {
                    final var fy = y;
                    newSquare = squareMaps.stream().filter(s -> s.square.startX == startX && s.square.startY == fy).findFirst();
                }
            } else {
                for (var y = fromStartY; y >= toStartY && newSquare.isEmpty(); y -= squareSize) {
                    final var fy = y;
                    newSquare = squareMaps.stream().filter(s -> s.square.startX == startX && s.square.startY == fy).findFirst();
                }
            }
            return newSquare;
        }

        private Optional<SquareMap> findByY(int startY, int fromStartX, int toStartX, int squareSize) {
            if (fromStartX < 0) return Optional.empty();
            Optional<SquareMap> newSquare = Optional.empty();
            if (fromStartX < toStartX) {
                for (var x = fromStartX; x <= toStartX && newSquare.isEmpty(); x += squareSize) {
                    final var fx = x;
                    newSquare = squareMaps.stream().filter(s -> s.square.startX == fx && s.square.startY == startY).findFirst();
                }
            } else {
                for (var x = fromStartX; x >= toStartX && newSquare.isEmpty(); x -= squareSize) {
                    final var fx = x;
                    newSquare = squareMaps.stream().filter(s -> s.square.startX == fx && s.square.startY == startY).findFirst();
                }
            }
            return newSquare;
        }
    }

    private static final class SquareMap {
        private final Side side;
        private final Square square;
        private final char[][] map;
        private SquareMap(Side side, Square square, char[][] map) {
            this.side = side;
            this.square = square;
            this.map = map;
        }
    }

    private static final class Square {
        private final Coordinate leftBottom;
        private final Coordinate rightBottom;
        private final Coordinate leftTop;
        private final Coordinate rightTop;
        private final int startX;
        private final int startY;
        private Square(Coordinate leftBottom, Coordinate rightBottom, Coordinate leftTop, Coordinate rightTop, int startX, int startY) {
            this.leftBottom = leftBottom;
            this.rightBottom = rightBottom;
            this.leftTop = leftTop;
            this.rightTop = rightTop;
            this.startX = startX;
            this.startY = startY;
        }

        private boolean contains(Set<Coordinate> coordinates) {
            return Set.of(leftBottom, rightBottom, leftTop, rightTop).containsAll(coordinates);
        }

        private Set<Coordinate> getDirectionCoordinates(Direction direction) {
            return switch (direction) {
                case LEFT -> Set.of(leftTop, leftBottom);
                case RIGHT -> Set.of(rightTop, rightBottom);
                case UP -> Set.of(leftTop, rightTop);
                case DOWN -> Set.of(leftBottom, rightBottom);
            };
        }

        private Direction getDirection(Set<Coordinate> coordinates) {
            if (Set.of(leftTop, rightTop).equals(coordinates)) { return Direction.DOWN; }
            if (Set.of(rightTop, rightBottom).equals(coordinates)) { return Direction.LEFT; }
            if (Set.of(rightBottom, leftBottom).equals(coordinates)) { return Direction.UP; }
            if (Set.of(leftBottom, leftTop).equals(coordinates)) { return Direction.RIGHT; }
            
            throw new RuntimeException("Unsupported coordinates: " + coordinates);
        }

        @Override
        public String toString() {
            return String.format("Square[leftBottom=%s,rightBottom=%s,leftTop=%s,rightTop=%s,startX=%s,startY=%s]", leftBottom, rightBottom, leftTop, rightTop, startX, startY);
        }
    }

    private static final class Coordinate {
        private final int x;
        private final int y;
        private final int z;
        private Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("(%s,%s,%s)", x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            var otherCoordinate = (Coordinate) o;
            return otherCoordinate.x == x && otherCoordinate.y == y && otherCoordinate.z == z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private static final class CubeFace {
        private final Side side;
        private final Set<Coordinate> coordinates;
        
        private CubeFace(Side side, Set<Coordinate> coordinates) {
            this.side = side;
            this.coordinates = coordinates;
        }

        @Override
        public String toString() {
            return String.format("CubeFace[side=%s]{coordinates=%s}", side, coordinates);
        }
    }

    private enum Side {
        FRONT,
        LEFT,
        RIGHT,
        BACK,
        TOP,
        BOTTOM
    }

    private enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}