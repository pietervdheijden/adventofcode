const fs = require('fs');

function calculate(assignment1, example) {
  const file = example ? 'dataset.example.txt' : 'dataset.mypuzzle.txt'
  const data = fs.readFileSync(file, 'utf8');
  var openClosePairs = {
    '(': ')',
    '[': ']',
    '{': '}',
    '<': '>'
  };
  var errorChars = [];
  var completionStrings = [];
  data.split('\n').forEach((line) => {
    var stack = [];
    for (const char of line.split('')) {
      if (!['(','[','{','<',')',']','}','>'].includes(char)) {
        throw `Unsupported character: ${char}`;
      }
      if (openClosePairs[char] !== undefined) {
        stack.push(char)
      } else {
        if (openClosePairs[stack[stack.length-1]] != char) {
          errorChars.push(char);
          return;
        } else {
          stack.splice(stack.length-1,1);
        }
      }
    };
    var completionString = '';
    while (stack.length > 0) {
      completionString += openClosePairs[stack[stack.length-1]];
      stack.splice(stack.length-1,1);
    }
    completionStrings.push(completionString);
  });

  if (assignment1) {
    var errorCharScore = {
      ')': 3,
      ']': 57,
      '}': 1197,
      '>': 25137
    };
    return errorChars.map(char => errorCharScore[char]).reduce((a,b) => a+b,0);
  } else {
    var incompleteCharScore = {
      ')': 1,
      ']': 2,
      '}': 3,
      '>': 4
    };
    var scores = [];
    completionStrings.forEach(string => {
      var score = 0;
      string.split('').forEach(char => {
        score = (score * 5) + incompleteCharScore[char];
      });
      scores.push(score)
    });
    return scores.sort((a,b) => a-b)[(scores.length-1)/2];
  }
}

console.log(`Assigment 1: ${calculate(true, true)} \t (example)`);
console.log(`Assigment 1: ${calculate(true, false)} \t (my puzzle)`);
console.log(`Assigment 2: ${calculate(false, true)} \t (example)`);
console.log(`Assigment 2: ${calculate(false, false)} \t (my puzzle)`);
