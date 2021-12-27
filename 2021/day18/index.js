import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var snailfishNumbers = [];
  data.split('\n').forEach(line => {
    var snailfishNumber = [];
    var depth = 0;
    for (var c of line.split('')) {
      if (c == '[')
        depth++;
      else if (c == ']')
        depth--;
      else if (c == ',')
        ; //skip
      else
        snailfishNumber.push({depth: depth, value: parseInt(c)})
    }
    snailfishNumbers.push(snailfishNumber);
  });

  // generate combinations
  var combinations = [];
  if (assignment1) {
    // assignment1: sum all numbers
    combinations.push(snailfishNumbers);
  } else {
    // assignment2: sum all permutations of 2 numbers
    for (var i = 0; i < snailfishNumbers.length; i++) {
      for (var j = 0; j < snailfishNumbers.length; j++) {
        if (i == j) continue;
        combinations.push([snailfishNumbers[i], snailfishNumbers[j]])
      }
    }
  }

  var maxMagnitude = 0;
  for (var combination of combinations) {
    var sum = [];
    for (var snailfishNumber of combination) {
      // increase depth of existing numbers
      for (var i = 0; i < sum.length; i++) {
        sum[i].depth++;
      }
      
      // add
      sum.push(...snailfishNumber.map(function(sn) {
        return {
          // do not increase depth if it's the first number
          depth: (sn.depth + (sum.length == 0 ? 0 : 1)),
          value: sn.value
        };
      }));
      
      // reduce
      var changed = true;
      while (changed) {
        changed = false;

        // explode
        for (var i = 0; i < sum.length; i++) {
          // check depth == 5 instead of depth == 4 to include last [ for the number
          if (sum[i].depth == 5) {
            if (sum[i-1]) sum[i-1].value += sum[i].value;
            if (sum[i+2]) sum[i+2].value += sum[i+1].value;
            sum.splice(i,2, {depth: sum[i].depth-1, value: 0});
            changed = true;
            break;
          }
        }

        if (changed) continue;

        // split
        for (var i = 0; i < sum.length; i++) {
          if (sum[i].value > 9) {
            var leftSplit = Math.floor(sum[i].value / 2);
            var rightSplit = Math.ceil(sum[i].value / 2);
            sum.splice(i, 1, {depth: sum[i].depth+1, value: leftSplit}, {depth: sum[i].depth+1, value: rightSplit})
            changed = true;
            break;
          }
        }
      }
    }

    // calculate magnitude
    while (sum.length > 1) {
      var maxDepth = Math.max(...sum.map(r => r.depth));
      for (var i = 0; i < sum.length; i++) {
        if (sum[i].depth == maxDepth) {
          var value = sum[i].value * 3 + sum[i+1].value * 2;
          sum.splice(i, 2, {depth: sum[i].depth-1, value: value});
          break;
        }
      }
    }
    maxMagnitude = Math.max(sum[0].value, maxMagnitude);
  }
  return maxMagnitude;
}

console.log(`Assigment 1 (example): expected: 4140, actual: ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): expected: 4072, actual: ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): expected: 3993, actual: ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): expected: 4483, actual: ${calculate(false, 'mypuzzle')}`);
