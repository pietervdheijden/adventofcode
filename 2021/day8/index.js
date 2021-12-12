const fs = require('fs')

function calculate(useExampleDataset, assignment1) {
  const file = useExampleDataset ? 'dataset-example.txt' : 'dataset.txt'
  const data = fs.readFileSync(file, 'utf8');
  var decodedOutputValues = [];
  data.split('\n').forEach((line) => {
    var allValues = line.split(' | ').join(' ').split(' ') // signal and output values
      .map(v => v.split('').sort().join('')) // alphabetically sort strings
      .filter((item, index, array) => array.indexOf(item) === index); // filter dups
    
    // Business rules:
    // - key1 has length 2
    // - key4 has length 4
    // - key7 has length 3
    // - key8 has length 7
    // - key9 has length 6, contains key4
    // - key0 has length 6, contains key1, does not equal to key 9
    // - key6 has length 6, does not equal to key 0 or 9
    // - key3 has length 5, contains key 1
    // - key5 has length 5, is contained by key 6
    // - key2 has length 5, does not equal to key 3 or 5
    var key1 = allValues.filter(v => v.length == 2)[0];
    var key4 = allValues.filter(v => v.length == 4)[0];
    var key7 = allValues.filter(v => v.length == 3)[0];
    var key8 = allValues.filter(v => v.length == 7)[0];
    var key9 = allValues.filter(v => v.length == 6 && key4.split('').every(k => v.split('').includes(k)))[0];
    var key0 = allValues.filter(v => v.length == 6 && key1.split('').every(k => v.split('').includes(k)) && v != key9)[0];
    var key6 = allValues.filter(v => v.length == 6 && v != key0 && v != key9)[0];
    var key3 = allValues.filter(v => v.length == 5 && key1.split('').every(k => v.split('').includes(k)))[0];
    var key5 = allValues.filter(v => v.length == 5 && v.split('').every(k => key6.split('').includes(k)))[0];
    var key2 = allValues.filter(v => v.length == 5 && v != key3 && v != key5)[0];
    var lookup = {};
    lookup[key0] = '0';
    lookup[key1] = '1';
    lookup[key2] = '2';
    lookup[key3] = '3';
    lookup[key4] = '4';
    lookup[key5] = '5';
    lookup[key6] = '6';
    lookup[key7] = '7';
    lookup[key8] = '8';
    lookup[key9] = '9';

    var decodedOutputValue = '';
    line.split(' | ')[1].split(' ').forEach(outputValue => {
      decodedOutputValue += lookup[outputValue.split('').sort().join('')];
    });
    decodedOutputValues.push(parseInt(decodedOutputValue))
  });
  return assignment1 ? 
      decodedOutputValues.join('').split('').filter(digit => ['1','4','7','8'].includes(digit)).length
    : decodedOutputValues.reduce((a,b) => a+b, 0);
}

console.log(`Assigment 1: ${calculate(true, true)} \t (dataset: example)`);
console.log(`Assigment 1: ${calculate(false, true)} \t (dataset: my puzzle input)`);
console.log(`Assigment 2: ${calculate(true, false)} \t (dataset: example)`);
console.log(`Assigment 2: ${calculate(false, false)} \t (dataset: my puzzle input)`);
