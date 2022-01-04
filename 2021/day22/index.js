import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var rebootSteps = data.split('\n').map(function(row) {
    var matches = row.match('(on|off) x=([-]?[0-9]+)\.\.([-]?[0-9]+),y=([-]?[0-9]+)\.\.([-]?[0-9]+),z=([-]?[0-9]+)\.\.([-]?[0-9]+)')
    var on = matches[1] == 'on';
    var x1 = parseInt(matches[2]); var x2 = parseInt(matches[3]);
    var y1 = parseInt(matches[4]); var y2 = parseInt(matches[5]);
    var z1 = parseInt(matches[6]); var z2 = parseInt(matches[7]);
    return {
      on: on,
      cubeRegion: {
        x1: Math.min(x1, x2), x2: Math.max(x1, x2),
        y1: Math.min(y1, y2), y2: Math.max(y1, y2),
        z1: Math.min(z1, z2), z2: Math.max(z1, z2),
      }
    };
  });

  // execute reboot steps
  var cubeRegions = []; // only contains cube regions which are "on"
  for (var rebootStep of rebootSteps) {
    var rsCubeRegion = rebootStep.cubeRegion;

    if (assignment1
      && (  rsCubeRegion.x1 < -50 || rsCubeRegion.x2 > 50
        ||  rsCubeRegion.y1 < -50 || rsCubeRegion.y2 > 50
        ||  rsCubeRegion.z1 < -50 || rsCubeRegion.z2 > 50))
        continue; // assignment 1: skip regions where x,y,z are >50 or <-50

    if (rebootStep.on) { // on: add rsCubeRegion to cubeRegions, if needed split (new) rsCubeRegion
      var newCubeRegions = [rsCubeRegion];
      for (var currentCubeRegion of cubeRegions) {
        for (var i = 0; i < newCubeRegions.length; i++) {
          var newCubeRegion = newCubeRegions[i];
          if (  currentCubeRegion.x1 > newCubeRegion.x2 || currentCubeRegion.y1 > newCubeRegion.y2 || currentCubeRegion.z1 > newCubeRegion.z2
            ||  currentCubeRegion.x2 < newCubeRegion.x1 || currentCubeRegion.y2 < newCubeRegion.y1 || currentCubeRegion.z2 < newCubeRegion.z1)
            continue; // skip no overlap
          newCubeRegions.splice(i, 1);
          newCubeRegions.push(...splitCube(newCubeRegion, currentCubeRegion));
          i--;
        }
      }
      cubeRegions.push(...newCubeRegions);
    } else { // off: remove rsCubeRegion from cubeRegions, if needed split (current) cubeRegions
      var newCubeRegions = [];
      for (var currentCubeRegion of cubeRegions) {
        if (  currentCubeRegion.x1 > rsCubeRegion.x2 || currentCubeRegion.y1 > rsCubeRegion.y2 || currentCubeRegion.z1 > rsCubeRegion.z2
          ||  currentCubeRegion.x2 < rsCubeRegion.x1 || currentCubeRegion.y2 < rsCubeRegion.y1 || currentCubeRegion.z2 < rsCubeRegion.z1) {
          newCubeRegions.push(currentCubeRegion); // no overlap, add region
          continue;
        }
        newCubeRegions.push(...splitCube(currentCubeRegion, rsCubeRegion)); // overlap, split region
      }
      cubeRegions = newCubeRegions;
    }
  }
  return cubeRegions.map(c => (c.x2-c.x1+1) * (c.y2-c.y1+1) * (c.z2 - c.z1+1)).reduce((a,b) => a + b, 0);
}

// remove overlappingCube from cube by splitting cube into smaller cubes (max 9)
function splitCube(cube, overlappingCube) {
  var valuesX = [
    {x1: cube.x1, x2: overlappingCube.x1 - 1},                                            // left
    {x1: Math.max(cube.x1,overlappingCube.x1), x2: Math.min(cube.x2,overlappingCube.x2)}, // middle
    {x1: overlappingCube.x2 + 1, x2: cube.x2},                                            // right
  ];
  var valuesY = [
    {y1: cube.y1, y2: overlappingCube.y1 - 1},                                            // bottom
    {y1: Math.max(cube.y1,overlappingCube.y1), y2: Math.min(cube.y2,overlappingCube.y2)}, // center
    {y1: overlappingCube.y2 + 1, y2: cube.y2},                                            // top
  ];
  var valuesZ = [
    {z1: cube.z1, z2: overlappingCube.z1 - 1},                                            // front
    {z1: Math.max(cube.z1,overlappingCube.z1), z2: Math.min(cube.z2,overlappingCube.z2)}, // center
    {z1: overlappingCube.z2 + 1, z2: cube.z2},                                            // back
  ];
  var splitCubes = [];
  for (var valueX of valuesX) {
    for (var valueY of valuesY) {
      for (var valueZ of valuesZ) {
        if (valueX.x1 > valueX.x2 || valueY.y1 > valueY.y2 || valueZ.z1 > valueZ.z2)
          continue; // skip invalid range
        if (  valueX.x1 >= overlappingCube.x1 && valueX.x2 <= overlappingCube.x2
          &&  valueY.y1 >= overlappingCube.y1 && valueY.y2 <= overlappingCube.y2
          &&  valueZ.z1 >= overlappingCube.z1 && valueZ.z2 <= overlappingCube.z2)
          continue; // skip full overlap
        splitCubes.push({x1: valueX.x1, x2: valueX.x2, y1: valueY.y1, y2: valueY.y2, z1: valueZ.z1, z2: valueZ.z2}); // add no overlap
      }
    }
  }
  return splitCubes;
}

console.log(`Assigment 1 (example): expected: 39, actual: ${calculate(true, 'example0')}`);
console.log(`Assigment 1 (example): expected: 590784, actual: ${calculate(true, 'example1')}`);
console.log(`Assigment 1 (mypuzzle): expected: 596598, actual: ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): expected: 2758514936282235, actual: ${calculate(false, 'example2')}`);
console.log(`Assigment 2 (mypuzzle): expected: 1199121349148621, actual: ${calculate(false, 'mypuzzle')}`);
