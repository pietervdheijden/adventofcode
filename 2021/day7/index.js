const fs = require('fs')

function calculateMinFuel(useExampleDataset, assignment1) {
  const file = useExampleDataset ? 'dataset-example.txt' : 'dataset.txt'
  const data = fs.readFileSync(file, 'utf8');
  var crabPositions = data.split(',').map(x => parseInt(x));
  var minPosition = Math.min(...crabPositions);
  var maxPosition = Math.max(...crabPositions);
  
  var minFuelPosition = -1;
  var minFuel = Number.MAX_SAFE_INTEGER;
  for (var position = minPosition; position <= maxPosition; position++) {
    var fuel = 0;
    crabPositions.forEach(p => {
      var steps = Math.abs(p - position);
      fuel += assignment1 ? steps : steps * (steps + 1) / 2;
    });
    if (fuel < minFuel) {
      minFuelPosition = position;
      minFuel = fuel;
    }
  }
  return minFuel;
}

console.log(`Assigment 1: ${calculateMinFuel(true, true)} \t (dataset: example)`);
console.log(`Assigment 1: ${calculateMinFuel(false, true)} \t (dataset: my puzzle input)`);
console.log(`Assigment 2: ${calculateMinFuel(true, false)} \t (dataset: example)`);
console.log(`Assigment 2: ${calculateMinFuel(false, false)} \t (dataset: my puzzle input)`);