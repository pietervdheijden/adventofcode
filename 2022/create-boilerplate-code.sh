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
        calculate(true, \"example.txt\", 0);
        // calculate(true, \"mypuzzle.txt\", 0);
        // calculate(false, \"example.txt\", 0);
        // calculate(false, \"mypuzzle.txt\", 0);
    }    

    private static void calculate(Boolean assignment1, String dataset, int expected) throws IOException {
        var puzzle = Files.readString(Paths.get(\"datasets/\" + dataset));

        // TODO

        // Print results
        var actual = assignment1 ? 0 : 0;
        System.out.println(String.format(\"Assignment %s - dataset=%s [%s]\\\t: expected=%s, actual=%s\", 
            assignment1 ? \"1\" : \"2\",
            dataset,
            expected == actual ? \"PASSED\" : \"FAILED\",
            expected,
            actual));
    }
}" > $dayDir/App.java

# Create datasets
mkdir $dayDir/datasets
echo "TODO" > $dayDir/datasets/example.txt
echo "TODO" > $dayDir/datasets/mypuzzle.txt
