const fs = require('fs');

function calculate(assignment1, example) {
  const file = example ? 'dataset.example.txt' : 'dataset.mypuzzle.txt'
  const data = fs.readFileSync(file, 'utf8');
  var octopuses = [];
  data.split('\n').forEach((line) => {
    octopuses.push(line.split('').map(v => parseInt(v)));
  });
  
  var steps = 0;
  var flashes = 0;
  while (true) {
    steps++;
    // increase each octopus by 1
    for (var rowIndex = 0; rowIndex < octopuses.length; rowIndex++) {
      for (var columnIndex = 0; columnIndex < octopuses[rowIndex].length; columnIndex++) {
        octopuses[rowIndex][columnIndex]++
      }
    }
    // run flashes
    while ([].concat.apply([], octopuses).filter(o => o > 9).length) {
      for (var rowIndex = 0; rowIndex < octopuses.length; rowIndex++) {
        for (var columnIndex = 0; columnIndex < octopuses[rowIndex].length; columnIndex++) {
          if (octopuses[rowIndex][columnIndex] > 9) {
            increment(octopuses, rowIndex-1, columnIndex-1); // left top
            increment(octopuses, rowIndex-1, columnIndex);   // top
            increment(octopuses, rowIndex-1, columnIndex+1); // right top
            increment(octopuses, rowIndex,   columnIndex+1); // right
            increment(octopuses, rowIndex+1, columnIndex+1); // right bottom
            increment(octopuses, rowIndex+1, columnIndex);   // bottom
            increment(octopuses, rowIndex+1, columnIndex-1); // left bottom
            increment(octopuses, rowIndex,   columnIndex-1); // left
            octopuses[rowIndex][columnIndex] = 0;
            flashes++;
          }
        }
      }
    }
    // return
    if (assignment1) {
      if (steps == 100) return flashes;
    } else { // assignment 2
      if ([].concat.apply([], octopuses).filter(o => o != 0).length == 0) return steps;
    } 
  }
}

function increment(octopuses, rowIndex, columnIndex) {
  if (rowIndex >= 0 
    && columnIndex >= 0
    && octopuses.length > rowIndex
    && octopuses[rowIndex].length > columnIndex
    && octopuses[rowIndex][columnIndex] != 0 // filter octopuses that have already flashed this step  
  ) {
    octopuses[rowIndex][columnIndex]++;
  }
}

console.log(`Assigment 1: ${calculate(true, true)} \t (example)`);
console.log(`Assigment 1: ${calculate(true, false)} \t (my puzzle)`);
console.log(`Assigment 2: ${calculate(false, true)} \t (example)`);
console.log(`Assigment 2: ${calculate(false, false)} \t (my puzzle)`);
