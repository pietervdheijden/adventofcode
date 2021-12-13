const fs = require('fs')

function getDataset(useExampleDataset) {
  const file = useExampleDataset ? 'dataset-example.txt' : 'dataset.txt'
  const data = fs.readFileSync(file, 'utf8');
  
  var lanternFishes = data.split(',').map(x => parseInt(x));
  var lanternFishGroups = [...Array(10)].map(() => 0);
  lanternFishes.forEach(fish => {
    lanternFishGroups[fish]++
  });

  return {
    lanternFishGroups: lanternFishGroups
  };
}

function reproduce(days, useExampleDataset) {
  var dataset = getDataset(useExampleDataset);
  var lanternFishGroups = dataset.lanternFishGroups;
  while (days > 0) {
    for (var groupIndex = 0; groupIndex < lanternFishGroups.length; groupIndex++) {
      var lanternFishes = lanternFishGroups[groupIndex];
      lanternFishGroups[groupIndex] = 0;
      if (groupIndex == 0) {
        lanternFishGroups[7] += lanternFishes;
        lanternFishGroups[9] = lanternFishes; // add to last group
      } else {
        lanternFishGroups[groupIndex-1] = lanternFishes;
      }
    }
    days--;
  }
  return lanternFishGroups.reduce((a,b) => a + b, 0);
}

function assignment1(useExampleDataset) {
  var result = reproduce(80, useExampleDataset);
  console.log(`Assigment 1: ${result} (dataset: ${useExampleDataset ? 'example' : 'personal'})`);
}

function assignment2(useExampleDataset) {
  var result = reproduce(256, useExampleDataset);
  console.log(`Assigment 2: ${result} (dataset: ${useExampleDataset ? 'example' : 'personal'})`);
}

assignment1(false);
assignment2(false);