import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var rows = data.split('\n');
  var amphipods = [];
  var amphipodId = 0;
  var y = 0;
  rows.forEach(function(row) {
    var roomSlice = row.match('#([A-D])#([A-D])#([A-D])#([A-D])#');
    if (!roomSlice) return;
    y++;
    for (var i = 1; i <= 4; i++) {
      var amphipod = roomSlice[i];
      amphipods.push({
        id: ++amphipodId,
        type: amphipod,
        x: i*2,
        y: y,
        targetX: {A: 2, B: 4, C: 6, D: 8}[amphipod],
        energyPerStep: {A: 1, B: 10, C: 100, D: 1000}[amphipod],
      });
    }
  });

  // organize and return (min) energy
  var result = organize(amphipods, y, {});
  if (debug){
    console.log('Organize result:')
    console.log(`- energy: ${result.energy}`)
    console.log('- path:');
    console.log(result.path);
  }
  return result.energy;
}

function organize(amphipods, maxY, cache) { 
  if (amphipods.every(a => a.x == a.targetX))
    return {energy: 0, path: []}; // return when amphipods are organized correctly
  var cacheKey = JSON.stringify(amphipods);
  if (cache[cacheKey] !== undefined)
    return cache[cacheKey]; // return from cache if available
  
  // get moves
  var moves = [];
  var hallwaySpots = [0,1,3,5,7,9,10];
  for (var amphipod of amphipods) {
    var i = amphipods.findIndex(value => value.id == amphipod.id);
    if (amphipod.x == amphipod.targetX && amphipods.every(a => a.x != amphipod.x || a.x == a.targetX))
      continue; // skip when amphipod is already in the correct room
    if (amphipods.some(a => a.x == amphipod.x && a.y < amphipod.y))
      continue; // skip when amphipod is blocked by another amphipod in the same room    

    // get moves to room
    if (amphipods.every(a => a.x != amphipod.targetX || a.x == a.targetX) // check that the target room doesn't contain "wrong" amphipods
      && hallwaySpots
        .filter(x => (x >= amphipod.targetX && x <= amphipod.x) || (x <= amphipod.targetX && x >= amphipod.x))
        .every(x => !amphipods.some(a => a.x == x && a.id != amphipod.id))) // check that all intermediate hallwaySpots are free (excluding current amphipod)
    {
      var y = amphipods.some(a => a.x == amphipod.targetX)
        ? Math.min(...amphipods.filter(a => a.x == amphipod.targetX).map(a => a.y)) - 1
        : maxY;
      moves.push({index: i, x: amphipod.targetX, y: y});
      continue; // if the amphipod can move to a room, then don't evaluate other options
    }

    // get moves to hallway
    for (var hallwaySpot of hallwaySpots) {
      if (amphipod.y > 0
        && hallwaySpots
          .filter(x => (x >= hallwaySpot && x <= amphipod.x) || (x <= hallwaySpot && x >= amphipod.x))
          .every(x => !amphipods.some(a => a.x == x && a.id != amphipod.id))) // check that all intermediate hallwaySpots are free (excluding current amphipod));
        moves.push({index: i, x: hallwaySpot, y: 0});
    }
  }

  // execute moves
  var minEnergy = Infinity;
  var minPath = [];
  for (var move of moves) {
    var amphipodsCopy = JSON.parse(JSON.stringify(amphipods));
    var amphipod = amphipodsCopy[move.index];
    var energy = (Math.abs(amphipod.x - move.x) + amphipod.y + move.y) * amphipod.energyPerStep;
    var step = `move amphipod ${amphipod.type} from (${amphipod.x},${amphipod.y}) to (${move.x},${move.y}) with energy ${energy}`;
    amphipod.x = move.x;
    amphipod.y = move.y;
    var shortestPath = organize(amphipodsCopy, maxY, cache);
    energy += shortestPath.energy;
    if (energy < minEnergy) {
      minEnergy = energy;
      minPath = [step].concat(...shortestPath.path);
    }
  }

  // update cache and return
  cache[cacheKey] = {energy: minEnergy, path: minPath};
  return cache[cacheKey];
}

console.log(`Assigment 1 (example): expected: 12521, actual: ${calculate(true, 'example1.txt')}`);
console.log(`Assigment 1 (mypuzzle): expected: 19160, actual: ${calculate(true, 'mypuzzle1.txt')}`);
console.log(`Assigment 2 (example): expected: 44169, actual: ${calculate(false, 'example2.txt')}`);
console.log(`Assigment 2 (mypuzzle): expected: 47232, actual: ${calculate(false, 'mypuzzle2.txt')}`);