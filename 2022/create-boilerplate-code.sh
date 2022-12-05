# Script to create boilerplate code for AdventOfCode solutions
# Usage:
#   ./create-boilerplate-code.sh <day>
# Example:
#   ./create-boilerplate-code.sh 05
#   or
#   ./create-boilerplate-code.sh 23

# Get parameters
day=$1

# (Re)create directory
dayDir="day${day}"
rm -rf $dayDir
mkdir $dayDir

# Create README.md
echo "# 2022 - Day $day
Script:

\`\`\`bash
java App.java
\`\`\`" > $dayDir/README.md

# Create App.java
echo "import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String args[]) throws IOException {
        System.out.println(\"Assignment 1 - example: expected=TODO, actual=\" + calculate(\"example.txt\", true));
        System.out.println(\"Assignment 1 - mypuzzle: expected=TODO, actual=\" + calculate(\"mypuzzle.txt\", true));

        System.out.println(\"Assignment 2 - example: expected=TODO, actual=\" + calculate(\"example.txt\", false));
        System.out.println(\"Assignment 2 - mypuzzle: expected=TODO, actual=\" + calculate(\"mypuzzle.txt\", false));
    }    

    private static int calculate(String dataset, Boolean assignment1) throws IOException {
        var puzzle = Files.readString(Paths.get(\"datasets/\" + dataset));

        // TODO
        return 0;
    }
}" > $dayDir/App.java

# Create datasets
mkdir $dayDir/datasets
echo "TODO" > $dayDir/datasets/example.txt
echo "TODO" > $dayDir/datasets/mypuzzle.txt
