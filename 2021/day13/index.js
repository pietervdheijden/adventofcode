import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var dots = [];
  var foldInstructions = [];
  data.split('\n\n')[0].split('\n').forEach((line) => {
    var dot = line.split(',').map(d => parseInt(d));
    dots.push({x: dot[0], y: dot[1]});
  });
  data.split('\n\n')[1].split('\n').forEach((line) => {
    line = line.replace('fold along ', '');
    var fold = line.split('=');
    foldInstructions.push({direction: fold[0], position: parseInt(fold[1])});
  });

  // apply fold instructions
  for (var fold of foldInstructions) {
    if (fold.direction == 'x') {
      dots = dots.map(function(d) {
        var x = d.x;
        if (x < fold.position) return d;
        var newX = x - 2 * (x - fold.position);
        if (newX < 0) return;
        return {x: newX, y: d.y};
      });
    } else if (fold.direction == 'y') {
      dots = dots.map(function(d) {
        var y = d.y;
        if (y < fold.position) return d;
        var newY = y - 2 * (y - fold.position);
        if (newY < 0) return;
        return {x: d.x, y: newY};
      });
    } else {
      throw `Unsupported fold.direction: ${fold.direction}`;
    }
    dots = dots
      .map(d => JSON.stringify(d))
      .filter((value, index, array) => array.indexOf(value) === index)
      .map(d => JSON.parse(d));

    // return length for assignment 1
    if (assignment1) {
      return dots.length;
    }
  };

  // calculate code for assignment 2
  var maxX = Math.max(...dots.map(d => d.x));
  var maxY = Math.max(...dots.map(d => d.y));
  var paper = [];
  for (var y = 0; y <= maxY; y++) {
    paper.push([]);
      for (var x = 0; x <= maxX; x++) {
        paper[y][x] = dots.some(d => d.x == x && d.y == y) ? '#' : ' '
    }
  }
  return paper.map(row => row.join('')).join('\n');
}
console.log(`Assigment 1 (example): ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): \n${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): \n${calculate(false, 'mypuzzle')}`);
