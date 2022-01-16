import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var numbers = data.split('\n').map(n => parseInt(n));

  if (assignment1) { // assignment1: count how often the number increased
    var count = 0;
    for (var i = 1; i < numbers.length; i++) {
      if (numbers[i] > numbers[i-1]) count++
    }
    return count;
  } else { // assignment2: count how often the three-measurement window increased
    var count = 0;
    for (var i = 3; i < numbers.length; i++) {
      var currentSlidingWindow = numbers[i] + numbers[i-1] + numbers[i-2];
      var previousSlidingWindow = numbers[i-1] + numbers[i-2] + numbers[i-3];
      if (currentSlidingWindow > previousSlidingWindow) count++
    }
    return count;
  }
}

console.log(`Assigment 1 (mypuzzle): expected: 7, actual: ${calculate(true, 'example.txt')}`);
console.log(`Assigment 1 (mypuzzle): expected: 1715, actual: ${calculate(true, 'mypuzzle.txt')}`);
console.log(`Assigment 2 (example): expected: 5, actual: ${calculate(false, 'mypuzzle.txt')}`);
console.log(`Assigment 2 (mypuzzle): expected: 1739, actual: ${calculate(false, 'mypuzzle.txt')}`);
