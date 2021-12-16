import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var polymer = data.split('\n\n')[0];
  var pairInsertionRules = {};
  data.split('\n\n')[1].split('\n').forEach((rule) => {
    rule = rule.split(' -> ');
    pairInsertionRules[rule[0]] = rule[1];
  });

  // get polymer pairs
  var polymerPairs = {};
  for (var elementIndex = 1; elementIndex < polymer.length; elementIndex++) {
    var pair = polymer[elementIndex-1] + polymer[elementIndex];
    polymerPairs[pair] = (polymerPairs[pair] ?? 0) + 1;
  }

  // apply polymerization
  var steps = assignment1 ? 10 : 40;
  while (steps > 0) {
    var newPolymerPairs = {};
    Object.keys(polymerPairs).forEach(pair => {
      var newElement = pairInsertionRules[pair];
      var newPair0 = pair[0] + newElement;
      var newPair1 = newElement + pair[1];
      newPolymerPairs[newPair0] = (newPolymerPairs[newPair0] ?? 0) + polymerPairs[pair];
      newPolymerPairs[newPair1] = (newPolymerPairs[newPair1] ?? 0) + polymerPairs[pair];
    });
    polymerPairs = newPolymerPairs;
    steps--
  }

  // generate counts and return results
  var counts = Object.keys(polymerPairs).reduce((total, value) => {
    total[value[0]] = (total[value[0]] || 0) + polymerPairs[value];
    return total;
  }, {});
  counts[polymer[polymer.length-1]]++; // increment last polymer by 1
  var minCount = Math.min(...Object.values(counts));
  var maxCount = Math.max(...Object.values(counts));
  return maxCount - minCount;
}


console.log(`Assigment 1 (example): ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): ${calculate(false, 'mypuzzle')}`);
