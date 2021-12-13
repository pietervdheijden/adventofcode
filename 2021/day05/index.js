const fs = require('fs')

function getDataset()
{
  const data = fs.readFileSync('dataset.txt', 'utf8');
  // const data = fs.readFileSync('dataset-example.txt', 'utf8');
  
  var lines = [];
  data.split(/\r?\n/).forEach(function(row) {
    var coordinates = row.split(' -> ').join(',').split(',').map(c => parseInt(c));

    lines.push({
      x1: coordinates[0],
      y1: coordinates[1],
      x2: coordinates[2],
      y2: coordinates[3]
    })

  });
  return lines;
}

function getGrid(lines, includeDiagonals) {
  // Initialize grid
  var maxX = Math.max.apply(Math, lines.map(function(line) { return Math.max(line.x1, line.x2) + 1; }));
  var maxY = Math.max.apply(Math, lines.map(function(line) { return Math.max(line.y1, line.y2) + 1; }));
  var grid = [...Array(maxX)].map(x => [...Array(maxY)].map(() => 0));
  
  // Populate grid
  lines.forEach(line => {
    var x1 = line.x1;
    var x2 = line.x2;
    var y1 = line.y1;
    var y2 = line.y2;

    if (!includeDiagonals && x1 != x2 && y1 != y2) return;

    var stepX = 0;
    var stepY = 0;
    if (x1 > x2) stepX = -1;
    if (x1 < x2) stepX = 1;
    if (y1 > y2) stepY = -1;
    if (y1 < y2) stepY = 1;

    var x = x1;
    var y = y1;
    grid[x][y]++;
    while (x != x2 || y != y2) {
      x += stepX;
      y += stepY;
      grid[x][y]++;
    }
  });
  return grid;
}

function assignment1() {
  var dataset = getDataset();
  var grid = getGrid(dataset, false);
  var score = [].concat.apply([], grid).filter(x => x > 1).length;

  console.log('Assignment 1:');
  console.log(`- Score: ${score}`);
}

function assignment2() {
  var dataset = getDataset();
  var grid = getGrid(dataset, true);
  var score = [].concat.apply([], grid).filter(x => x > 1).length;

  console.log('Assignment 2:');
  console.log(`- Score: ${score}`);
}

assignment1();
assignment2();