import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var risks = [];
  data.split('\n').forEach((line) => {
    risks.push(line.split('').map(risk => parseInt(risk)));
  });

  // assignment2: repeat risks 5 times in both horizontal and vertical direction
  if (!assignment1) {
    var repeatCount = 5;

    // add columns (x)
    for (var y = 0; y < risks.length; y++) {
      var risk = risks[y];
      for (var increment = 1; increment < repeatCount; increment++) {
        risks[y] = risks[y].concat(...risk.map(function(riskValue) {
          riskValue += increment;
          return (riskValue % 9 == 0) ? 9 : riskValue %= 9;
        }));
      }
    }

    // add rows (y)
    var originalRiskLength = risks.length;
    for (var increment = 1; increment < repeatCount; increment++) {
      for (var y = 0; y < originalRiskLength; y++) {
        var risk = risks[y];
        risks.push(risk.map(function(riskValue) {
          riskValue += increment;
          return (riskValue % 9 == 0) ? 9 : riskValue %= 9;
        }));
      }      
    }
  }

  // generate weighted graph
  var graph = {};
  var maxY = risks.length-1;
  var maxX = risks[0].length-1;
  for (var y = 0; y <= maxY; y++) {
    for (var x = 0; x <= maxX; x++) {
      var node = `(${x},${y})`;
      graph[node] = []
      if (x > 0) {
        var leftPosition = `(${x-1},${y})`;
        graph[node].push({node: leftPosition, edge: risks[y][x-1]});
      }
      if (x < maxX) {
        var rightPosition = `(${x+1},${y})`;
        graph[node].push({node: rightPosition, edge: risks[y][x+1]});
      }
      if (y > 0) {
        var topPosition = `(${x},${y-1})`;
        graph[node].push({node: topPosition, edge: risks[y-1][x]});
      }
      if (y < maxY) {
        var bottomPosition = `(${x},${y+1})`;
        graph[node].push({node: bottomPosition, edge: risks[y+1][x]});
      }
    }
  }

  // execute Dijkstra's algorithm
  // link: https://www.freecodecamp.org/news/dijkstras-shortest-path-algorithm-visual-introduction/
  // performance optimization: use dictionaries to efficiently lookup visited nodes.
  var startNode = '(0,0)';
  var endNode = `(${maxX},${maxY})`;
  var visitedNodes = [startNode];
  var visitedNodesDict = {}; 
  Object.keys(graph).forEach(n => visitedNodesDict[n] = (n == startNode));
  var distances = {};
  Object.keys(graph).forEach(n => distances[n] = (n == startNode) ? 0 : Infinity);
  while (visitedNodes.length > 0) {
    // find min node
    var minDistance = Infinity;
    var minNode;
    for (var visitedNode of visitedNodes) {
      for (var adjacentNode of graph[visitedNode]) {
        // skip visited nodes
        if (visitedNodesDict[adjacentNode.node]) continue;

        // update distances
        var distance = Math.min(distances[visitedNode] + adjacentNode.edge, distances[adjacentNode.node]);
        distances[adjacentNode.node] = distance;

        // update minDistance and minNode
        if (distance < minDistance) {
          minDistance = distance;
          minNode = adjacentNode.node;
        }
      }
    }

    // update min node
    if (minNode) {
      visitedNodes.push(minNode);
      visitedNodesDict[minNode] = true;
    }

    // performance optimization: remove visited nodes without unvisited adjacent nodes
    visitedNodes = visitedNodes.filter(n => graph[n].some(adjacentNode => !visitedNodesDict[adjacentNode.node]));
  }
  return distances[endNode];
}

console.log(`Assigment 1 (example): ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example): ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): ${calculate(false, 'mypuzzle')}`);
