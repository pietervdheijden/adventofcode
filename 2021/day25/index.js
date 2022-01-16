import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var cucumbers = data.split('\n').map(row => row.split(''));
  if (debug) {
    console.log('input:')
    console.log('- cucumbers:')
    console.log(cucumbers.map(row => row.join('')).join('\n'));
  }

  var steps = 0;
  while (true) {
    var changed = false;
    var newCucumbers = JSON.parse(JSON.stringify(cucumbers));

    // move east
    for (var i = 0; i < cucumbers.length; i++) {
      for (var j = 0; j < cucumbers[i].length; j++) {
        var adjacentJ = (j+1) % cucumbers[i].length;
        if (cucumbers[i][j] == '>' && cucumbers[i][adjacentJ] == '.') {
          newCucumbers[i][adjacentJ] = '>';
          newCucumbers[i][j] = '.';
          changed = true;
        }
      }
    }

    cucumbers = JSON.parse(JSON.stringify(newCucumbers));

    // move south
    for (var i = 0; i < cucumbers.length; i++) {
      for (var j = 0; j < cucumbers[i].length; j++) {
        var adjacentI = (i+1) % cucumbers.length;
        if (cucumbers[i][j] == 'v' && cucumbers[adjacentI][j] == '.') {
          newCucumbers[adjacentI][j] = 'v';
          newCucumbers[i][j] = '.';
          changed = true;
        }
      }
    }

    cucumbers = newCucumbers
    steps++;
    if (!changed) break;
  }

  if (debug) {
    console.log();
    console.log('output:')
    console.log('- cucumbers:')
    console.log(cucumbers.map(row => row.join('')).join('\n'));
    console.log(`- steps: ${steps}`);
  }
  
  return steps;
}

console.log(`Assigment 1 (example): expected: 58, actual: ${calculate(true, 'example.txt')}`);
console.log(`Assigment 1 (mypuzzle): expected: 419, actual: ${calculate(true, 'mypuzzle.txt')}`);
