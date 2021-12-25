import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var targetArea = data.replace('target area: x=', '').split(', y=');
  var x1 = Math.min(...targetArea[0].split('..').map(v => parseInt(v)));
  var x2 = Math.max(...targetArea[0].split('..').map(v => parseInt(v)));
  var y1 = Math.max(...targetArea[1].split('..').map(v => parseInt(v)));
  var y2 = Math.min(...targetArea[1].split('..').map(v => parseInt(v)));
  
  // calculate all velocity pairs
  // business rules:
  // - velocityX range: 0 < velocityX <= x2
  // - velocityY range: y2 <= velocityY < -y2
  var velocityPairs = [];
  var velocityY = y2;
  while (velocityY < -y2) {
    var y = 0;
    var stepY = 0;
    while (y > y1) {
      y += velocityY-stepY;
      stepY++;
    }
    while (y >= y2) {
      var velocityX = 1;
      while (velocityX <= x2) {
        var x = 0;
        var stepX = 0;
        var currentVelocityX = velocityX;
        while (stepX != stepY && currentVelocityX != 0) {
          x += currentVelocityX;
          stepX++
          if (currentVelocityX > 0) currentVelocityX--;
          if (currentVelocityX < 0) currentVelocityX++;
        }
        if (x1 <= x && x <= x2) {
          if (velocityPairs.filter(p => p.x == velocityX && p.y == velocityY).length == 0) {
            velocityPairs.push({x: velocityX, y: velocityY});
          }
        }
        velocityX++;
      }
      y += velocityY-stepY;
      stepY++;
    }
    velocityY++;
  }

  // return result
  return assignment1
    ? Math.max(...velocityPairs.map(p => (p.y * (p.y+1)) / 2)) // algorithm: n*(n-1)/2
    : velocityPairs.length;
}

console.log(`Assigment 1 (example): expected: 45, actual: ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): expected: 7875, actual: ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): expected: 112, actual: ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): expected: 2312, actual: ${calculate(false, 'mypuzzle')}`);
