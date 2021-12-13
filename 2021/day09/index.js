const fs = require('fs');

function calculate(assignment1, example) {
  const file = example ? 'dataset.example.txt' : 'dataset.mypuzzle.txt'
  const data = fs.readFileSync(file, 'utf8');
  var heightMap = [];
  data.split('\n').forEach((line) => {
    heightMap.push(line.split('').map(h => parseInt(h)));
  });
  var basins = [];
  for (var rowIndex = 0; rowIndex < heightMap.length; rowIndex++) {
    for (var columnIndex = 0; columnIndex < heightMap[rowIndex].length; columnIndex++) {
      var basin = getBasin(heightMap, rowIndex, columnIndex);
      if (basin.length != 0) {
        basins.push(basin);
      }
    }
  }
  return assignment1 ?
      basins.map(b => Math.min(...b)).reduce((a,b) => a+b+1,0)
    : basins.map(b => b.length).sort((a,b) => b-a).slice(0,3).reduce((a,b) => a*b);
}

function getBasin(heightMap, rowIndex, columnIndex) {
  var basin = [];
  if (rowIndex < 0 || heightMap.length <= rowIndex 
    || columnIndex < 0 || heightMap[heightMap.length-1].length <= columnIndex) {
    return basin;
  }
  var height = heightMap[rowIndex][columnIndex];
  if (height == 9) {
    return basin;
  }
  basin.push(height);
  heightMap[rowIndex][columnIndex] = 9;
  
  basin = basin.concat(getBasin(heightMap,rowIndex,columnIndex-1));
  basin = basin.concat(getBasin(heightMap,rowIndex,columnIndex+1));
  basin = basin.concat(getBasin(heightMap,rowIndex-1,columnIndex));
  basin = basin.concat(getBasin(heightMap,rowIndex+1,columnIndex));
  return basin;
}

console.log(`Assigment 1: ${calculate(true, true)} \t (example)`);
console.log(`Assigment 1: ${calculate(true, false)} \t (my puzzle)`);
console.log(`Assigment 2: ${calculate(false, true)} \t (example)`);
console.log(`Assigment 2: ${calculate(false, false)} \t (my puzzle)`);
