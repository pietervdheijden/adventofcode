import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var commands = data.split('\n').map(function(row) {
    var matches = row.match('(forward|down|up) ([0-9]+)')
    return {
      direction: matches[1],
      units: parseInt(matches[2])
    };
  });

  // assignment1
  // command definitions:
  // - forward X increases the horizontal position by X units.
  // - down X increases the depth by X units.
  // - up X decreases the depth by X units.
  if (assignment1) {
    var horizontalPosition = 0;
    var depth = 0;
    for (var command of commands) {
      if (command.direction == 'forward') horizontalPosition += command.units;
      if (command.direction == 'down') depth += command.units;
      if (command.direction == 'up') depth -= command.units;
    }
    return horizontalPosition * depth;
  }
  
  // assignment2
  // command definitions:
  // - down X increases your aim by X units.
  // - up X decreases your aim by X units.
  // - forward X does two things:
  //  - It increases your horizontal position by X units.
  //  - It increases your depth by your aim multiplied by X.
  var horizontalPosition = 0;
  var depth = 0;
  var aim = 0;
  for (var command of commands) {
    if (command.direction == 'forward') {
      horizontalPosition += command.units;
      depth += aim * command.units;
    }
    if (command.direction == 'down') aim += command.units;
    if (command.direction == 'up') aim -= command.units;
  }
  return horizontalPosition * depth;
}

console.log(`Assigment 1 (mypuzzle): expected: 150, actual: ${calculate(true, 'example.txt')}`);
console.log(`Assigment 1 (mypuzzle): expected: 1936494, actual: ${calculate(true, 'mypuzzle.txt')}`);
console.log(`Assigment 2 (example): expected: 900, actual: ${calculate(false, 'example.txt')}`);
console.log(`Assigment 2 (mypuzzle): expected: 1997106066, actual: ${calculate(false, 'mypuzzle.txt')}`);
