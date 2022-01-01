import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var scanners = [];
  data.split(/--- scanner [\d]+ ---\n/).forEach(function(block) {
    if (block == '') return;
    var scanner = {};
    scanner.beacons = [];
    scanner.distances = {};
    block.split('\n').map(c => c.split(',')).forEach(function(c) {
      if (c == '') return;
      scanner.beacons.push({
        coordinate: c.join(','),
        x: parseInt(c[0]),
        y: parseInt(c[1]),
        z: parseInt(c[2])
      });
    });
    scanners.push(scanner);
  });

  var scannerPositions = [];

  // add scanner 0 to scannerPositions
  scannerPositions.push({
    coordinate: '0,0,0',
    x: 0,
    y: 0,
    z: 0
  });

  // generate orientations
  //  the assignment states that there are 24 different orientations
  //  however, I'm not sure which 24 orientations are implied
  //  therefore, use all possible 48 permutations as orientations 
  var orientations = [];
  var axes = ['x', '-x', 'y', '-y', 'z', '-z'];
  for (var x of axes) {
    for (var y of axes) {
      for (var z of axes) {
        var absX = x.replace('-','');
        var absY = y.replace('-','');
        var absZ = z.replace('-','');
        if (absX != absY && absY != absZ && absX != absZ) {
          orientations.push({x: x, y: y, z: z});
        }
      }
    }
  }

  var nrOfScanners = scanners.length;
  // merge all scanners into scanner 0 and store (relative) positions into scannerPositions
  while (scanners.length > 1) {

    // generate all scanner orientations
    var scannerOrientations = {};
    for (var scannerIndex = 0; scannerIndex < scanners.length; scannerIndex++) {
      scannerOrientations[scannerIndex] = [];
      
      // transform beacons
      for (var orientationIndex = 0; orientationIndex < orientations.length; orientationIndex++) {
        scannerOrientations[scannerIndex][orientationIndex] = {beacons: []};
        for (var beacon of scanners[scannerIndex].beacons) {
          scannerOrientations[scannerIndex][orientationIndex].beacons.push(transform(beacon, orientations[orientationIndex]));
        }
      }

      // calculate distances to other beacons inside a scanner orientation
      for (var orientationIndex = 0; orientationIndex < scannerOrientations[scannerIndex].length; orientationIndex++) {
        var orientation = scannerOrientations[scannerIndex][orientationIndex];
        orientation.distancesPerBeacon = {};
        orientation.distances = [];
        for (var beacon of orientation.beacons) {
          orientation.distancesPerBeacon[beacon.coordinate] = [];
          for (var otherBeacon of orientation.beacons) {
            var distanceX = otherBeacon.x - beacon.x;
            var distanceY = otherBeacon.y - beacon.y;
            var distanceZ = otherBeacon.z - beacon.z;
            
            var distance = `${distanceX},${distanceY},${distanceZ}`;
            orientation.distancesPerBeacon[beacon.coordinate].push(distance);
            orientation.distances.push(distance);
          }
        }
      }
    }

    // merge first scanner (permutation) that has 12 overlapping beacons into scanner 0
    var scanner = scannerOrientations[0][0];
    outerLoop:
    for (var scannerIndex = 1; scannerIndex < scanners.length; scannerIndex++) {
      for (var orientationIndex = 0; orientationIndex < orientations.length; orientationIndex++) {
        var otherScanner = scannerOrientations[scannerIndex][orientationIndex];

        // performance optimization: check if the whole set of beacon distances has at least 12*11 matching distances
        // reasoning:
        //  overlapping detection reasons contain 12 overlapping beacons with 12 matching distances
        //  exclude distance 0,0,0 because each beacon has this distance
        var joinedScannerDistances = scanner.distances.concat(...otherScanner.distances).filter((value) => value != '0,0,0');
        var uniqueScannerDistances = [...new Set(joinedScannerDistances)];
        if (joinedScannerDistances.length - uniqueScannerDistances.length < 12 * 11) continue;
        
        for (var beacon of scanner.beacons) {
          for (var otherBeacon of otherScanner.beacons) {

            // check if beacons have at least 12 matching distances
            var joinedBeaconDistances = scanner.distancesPerBeacon[beacon.coordinate].concat(...otherScanner.distancesPerBeacon[otherBeacon.coordinate]);
            var uniqueBeaconDistances = [...new Set(joinedBeaconDistances)];
            if (joinedBeaconDistances.length - uniqueBeaconDistances.length >= 12) {
              // overlapping beacons detected, calculate relative scanner position and merge scanners

              // calculate other scanner (relative) position
              var distanceX = beacon.x - otherBeacon.x;
              var distanceY = beacon.y - otherBeacon.y;
              var distanceZ = beacon.z - otherBeacon.z;
              var scannerPosition = {
                coordinate: `${distanceX},${distanceY},${distanceZ}`,
                x: distanceX,
                y: distanceY,
                z: distanceZ
              }
              scannerPositions.push(scannerPosition);

              // merge other scanner's beacons into scanner 0
              for (var beacon of otherScanner.beacons) {
                var mergedBeaconX = beacon.x + scannerPosition.x;
                var mergedBeaconY = beacon.y + scannerPosition.y;
                var mergedBeaconZ = beacon.z + scannerPosition.z; 
                var mergedBeacon = {
                  coordinate: `${mergedBeaconX},${mergedBeaconY},${mergedBeaconZ}`,
                  x: mergedBeaconX,
                  y: mergedBeaconY,
                  z: mergedBeaconZ
                };
                if (scanner.beacons.filter(b => b.coordinate == mergedBeacon.coordinate).length == 0) {
                  scanners[0].beacons.push(mergedBeacon);
                }
              }

              // remove other scanner
              scanners.splice(scannerIndex, 1);
              break outerLoop;
            }
          }
        }
      }
    }

    console.log(`progress: ${scannerPositions.length-1}/${nrOfScanners-1}`);
    if (debug) {
      console.log('scanner overview:')
      for (var i = 0; i < scanners.length; i++) {
        console.log(`- scanner: ${i}, beacons: ${scanners[i].beacons.length}`)
      }
    }
  }

  // calculate max manhatten distance
  var maxManhattanDistance = 0;
  for (var scannerPosition of scannerPositions) {
    for (var otherScannerPosition of scannerPositions) {
      var distanceX = Math.max(scannerPosition.x, otherScannerPosition.x) - Math.min(scannerPosition.x, otherScannerPosition.x);
      var distanceY = Math.max(scannerPosition.y, otherScannerPosition.y) - Math.min(scannerPosition.y, otherScannerPosition.y);
      var distanceZ = Math.max(scannerPosition.z, otherScannerPosition.z) - Math.min(scannerPosition.z, otherScannerPosition.z);
      var manhattanDistance = distanceX + distanceY + distanceZ;
      maxManhattanDistance = Math.max(manhattanDistance, maxManhattanDistance);
    }
  }
  
  // print all beacons and scanner positions
  if (debug) {
    console.log('beacons:');
    console.log(scanners[0].beacons.sort((a,b) => a.x - b.x).map(b => b.coordinate).join('\n'));
    console.log(`scanner positions:`);
    console.log(scannerPositions);
  }

  return assignment1 
    ? scanners[0].beacons.length  // assignment 1: return nr of beacons
    : maxManhattanDistance        // assignment 2: return max manhattan distance
}

// transform orientation
function transform(coordinate, orientation) {
  var transformedCoordinate = {};

  // set x
  switch (orientation.x) {
    case 'x':
      transformedCoordinate.x = coordinate.x;
      break;
    case '-x':
      transformedCoordinate.x = -coordinate.x;
      break;
    case 'y':
      transformedCoordinate.x = coordinate.y;
      break;
    case '-y':
      transformedCoordinate.x = -coordinate.y;
      break;
    case 'z':
      transformedCoordinate.x = coordinate.z;
      break;
    case '-z':
      transformedCoordinate.x = -coordinate.z;
      break;
    default:
      throw `Unsupported orientation.x: ${template.x}`;
  }

  // set y
  switch (orientation.y) {
    case 'x':
      transformedCoordinate.y = coordinate.x;
      break;
    case '-x':
      transformedCoordinate.y = -coordinate.x;
      break;
    case 'y':
      transformedCoordinate.y = coordinate.y;
      break;
    case '-y':
      transformedCoordinate.y = -coordinate.y;
      break;
    case 'z':
      transformedCoordinate.y = coordinate.z;
      break;
    case '-z':
      transformedCoordinate.y = -coordinate.z;
      break;
    default:
      throw `Unsupported orientation.y: ${template.y}`;
  }

    // set z
  switch (orientation.z) {
    case 'x':
      transformedCoordinate.z = coordinate.x;
      break;
    case '-x':
      transformedCoordinate.z = -coordinate.x;
      break;
    case 'y':
      transformedCoordinate.z = coordinate.y;
      break;
    case '-y':
      transformedCoordinate.z = -coordinate.y;
      break;
    case 'z':
      transformedCoordinate.z = coordinate.z;
      break;
    case '-z':
      transformedCoordinate.z = -coordinate.z;
      break;
    default:
      throw `Unsupported orientation.z: ${template.z}`;
  }
  transformedCoordinate.coordinate = `${transformedCoordinate.x},${transformedCoordinate.y},${transformedCoordinate.z}`;
  return transformedCoordinate;
}

console.log(`Assigment 1 (example): expected: 79, actual: ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): expected: 419, actual: ${calculate(true, 'mypuzzle')}`);  
console.log(`Assigment 2 (example): expected: 3621, actual: ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): expected: 13210, actual: ${calculate(false, 'mypuzzle')}`);
