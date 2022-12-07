import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class App {
    public static void main(String args[]) throws IOException {
        System.out.println("Assignment 1 - example: expected=95437, actual=" + calculate("example.txt", true));
        System.out.println("Assignment 1 - mypuzzle: expected=1743217, actual=" + calculate("mypuzzle.txt", true));

        System.out.println("Assignment 2 - example: expected=24933642, actual=" + calculate("example.txt", false));
        System.out.println("Assignment 2 - mypuzzle: expected=8319096, actual=" + calculate("mypuzzle.txt", false));
    }    

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get("datasets/" + dataset));

        // Read directories
        var directories = new HashMap<String, Directory>();
        var currentDir = "";
        for (var command : puzzle.split("\\$ ")) {
            if (command.isEmpty())
                continue;
            if (command.startsWith("cd")) { // command is cd (change directory) => update currentDir
                var newDir = command.replace("cd ", "").replace("\n","");
                if (newDir.equals("..")) { // newDir == .., so move out
                    var currentDirSplit = currentDir.split("/");
                    currentDir = String.join("/", Arrays.copyOfRange(currentDirSplit, 0, currentDirSplit.length-1));
                    if (currentDir.isEmpty())
                        currentDir = "/";
                } else { // newDir != .., so move in
                    if (!newDir.equals("/") && !currentDir.equals("/"))
                        currentDir += "/";
                    currentDir += newDir;
                }
            } else { // command is ls (list directory contents) => store directory content in directories
                command = command.replaceFirst("ls\n","").trim();
                var size = 0;
                var subDirectories = new ArrayList<String>();
                for (var file : command.split("\n")) {
                    if (file.startsWith("dir")) { // file is a directory, so add to subDirectories
                        var newDir = currentDir;
                        if (!newDir.equals("/"))
                            newDir += "/";
                        newDir += file.split(" ")[1];
                        subDirectories.add(newDir);
                    } else { // file is a file, so sum the size
                        size += Integer.parseInt(file.split(" ")[0]);
                    }
                }
                directories.put(currentDir, new Directory(size, subDirectories));
            }
        }

        // Calculate solution
        if (assignment1) { // assignment 1: sum size for all directories with size <= 100000
            var sum = 0;
            for (var key : directories.keySet()) {
                var size = getSize(directories, key);
                if (size <= 100000)
                    sum += size;
            }
            return sum;
        } else { // assignment 2: get smallest directory which should be deleted such that 30000000 free space is available
            var freeSpace = 70000000 - getSize(directories, "/");
            var additionalRequiredFreeSpace = 30000000 - freeSpace;
            var minSize = Integer.MAX_VALUE;
            for (var key : directories.keySet()) {
                var size = getSize(directories, key);
                if (size > additionalRequiredFreeSpace)
                    minSize = Math.min(minSize, size);
            }
            return minSize;
        }
    }

    // Calculate the size of a directory by recursively adding the size of the subdirectories.
    private static int getSize(HashMap<String, Directory> directories, String directoryName) {
        var directory = directories.get(directoryName);
        var size = directory.getSize();
        for (var subDirectory : directory.getSubDirectories())
            size += getSize(directories, subDirectory);
        return size;
    }

    private static class Directory {
        private int size;
        private List<String> subDirectories;

        public Directory(int size, List<String> subDirectories) {
            this.size = size;
            this.subDirectories = subDirectories;
        }

        public int getSize() {
            return size;
        }

        public List<String> getSubDirectories() {
            return subDirectories;
        }
    }
}
