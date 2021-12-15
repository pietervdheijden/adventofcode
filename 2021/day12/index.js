const fs = require('fs');

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = fs.readFileSync(file, 'utf8');

  // parse dataset into dictionary caves
  var caves = {};
  data.split('\n').forEach((line) => {
    line.split('-').forEach(cave => {
      caves[cave] = {}
      caves[cave]["connections"] = [];
      caves[cave]["visits"] = (cave == 'start' || cave == 'end' || cave == cave.toLowerCase()) ? 1 : -1;
    })
  });
  data.split('\n').forEach((line) => {
    var caveLine = line.split('-')
    var cave0 = caveLine[0];
    var cave1 = caveLine[1];
    caves[cave0]["connections"] = caves[cave0]["connections"].concat(...[cave1])
      .filter((value, index, array) => array.indexOf(value) === index);
    caves[cave1]["connections"] = caves[cave1]["connections"].concat(...[cave0])
      .filter((value, index, array) => array.indexOf(value) === index);
  });

  // return results
  if (assignment1) {
    return findPaths(caves, ['start']).length;
  } else { // assignment 2
    var paths = [];
    for (var cave of Object.keys(caves).filter(c => c != 'start' && c != 'end' && c == c.toLowerCase())) {
      var cavesCopy = JSON.parse(JSON.stringify(caves)); // deep copy!
      delete cavesCopy[cave].visits; // js objects are not writable!
      cavesCopy[cave].visits = 2;
      paths = paths.concat(findPaths(cavesCopy, ['start']));
    }
    return paths
      .map(p => p.join(','))
      .filter((value, index, array) => array.indexOf(value) === index)
      .map(p => p.split(','))
      .length;
    }
}

function findPaths(caves, path) {
  var lastCave = path[path.length-1];
  var lastCaveVisits = caves[lastCave].visits;
  if (lastCaveVisits >= 1) {
    delete caves[lastCave].visits; // js objects are not writable!
    caves[lastCave].visits = lastCaveVisits-1;
  }
  if (lastCave == 'end') {
    return [path];
  }
  var nextCaves = caves[lastCave].connections.filter(nextCave => caves[nextCave].visits != 0);
  if (nextCaves.length == 0) {
    return []; // dead end
  }
  var paths = [];
  nextCaves.forEach(nextCave => {
    var newPath = path.concat(...[nextCave]);
    var newCaves = JSON.parse(JSON.stringify(caves)); // deep copy!
    paths = paths.concat(findPaths(newCaves, newPath));
  });
  return paths;
}

console.log(`Assigment 1: ${calculate(true, 'example1')} \t (example1)`);
console.log(`Assigment 1: ${calculate(true, 'example2')} \t (example2)`);
console.log(`Assigment 1: ${calculate(true, 'example3')} \t (example3)`);
console.log(`Assigment 1: ${calculate(true, 'mypuzzle')} \t (mypuzzle)`);
console.log(`Assigment 2: ${calculate(false, 'example1')} \t (example1)`);
console.log(`Assigment 2: ${calculate(false, 'example2')} \t (example2)`);
console.log(`Assigment 2: ${calculate(false, 'example3')} \t (example3)`);
console.log(`Assigment 2: ${calculate(false, 'mypuzzle')} \t (mypuzzle)`);
