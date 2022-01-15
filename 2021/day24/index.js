import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/mypuzzle.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var program = data.split('inp w\n').filter(i => i != '').map(function(instructions) {
    return instructions.split('\n').filter(i => i != '').map(function(instruction) {
      instruction = instruction.split(' ');
      return {
        operation: instruction[0],
        arg0: instruction[1],
        arg1: instruction[2] ?? ''
      };
    });
  });

  // find model number
  var modelNumber = findModelNumber(assignment1, program, 0, {w:0,x:0,y:0,z:0}, '');
  return parseInt(modelNumber);
}

function findModelNumber(max, program, index, dimensions, number) {
  if (index == program.length && dimensions.z == 0)
    return number;
  if (index == program.length && dimensions.z != 0)
    return null;

  var instructions = program[index];
  if (instructions.some(i => i.operation == 'div' && i.arg0 == 'z' && i.arg1 == '1')) {
    // type 1: z increases regardless of w, so try all w options
    // assignment1: find max => start with 9 and decrease
    // assignment2: find min => start with 1 and increase
    for (var w of [1,2,3,4,5,6,7,8,9].sort((a,b) => max ? b-a : a-b)) {
      var newDimensions = executeInstructions(instructions, dimensions, w);
      var modelNumber = findModelNumber(max, program, index+1, newDimensions, number + w);
      if (modelNumber) return modelNumber;
    }
  } else if (instructions.some(i => i.operation == 'div' && i.arg0 == 'z' && i.arg1 == '26')) {
    // type 2: MONAD requires that z has to be divided by 26 (!)
    // so try all w options, and only recurse if newZ = Math.trunc(z / 26);
    for (var w = 1; w <= 9; w++) {
      var newDimensions = executeInstructions(instructions, dimensions, w);
      if (newDimensions.z == Math.trunc(dimensions.z / 26)) {
        var modelNumber = findModelNumber(max, program, index+1, newDimensions, number + w);
        if (modelNumber) return modelNumber;
      }
    }
  } else {
    throw `Unsupported instructions!`
  }
}

function executeInstructions(instructions, dimensions, w) {
  // deep copy
  var newDimensions = JSON.parse(JSON.stringify(dimensions));
  newDimensions.w = w;

  // execute instructions
  for (var instruction of instructions) {
    var arg0 = instruction.arg0;
    var arg1 = parseInt(newDimensions[instruction.arg1] ?? instruction.arg1);
    switch(instruction.operation) {
      case 'add':
        newDimensions[arg0] += arg1;
        break;
      case 'mul':
        newDimensions[arg0] *= arg1;
        break;
      case 'div':
        if (arg1==0) throw `operation div does not support arg1==0`
        newDimensions[arg0] = Math.trunc(newDimensions[arg0] / arg1);
        break;
      case 'mod':
        if (newDimensions[arg0]<0) throw `operation mod does not support newDimensions[arg0]<0`
        if (arg1<=0) throw `operation mod does not support arg1<=0`
        newDimensions[arg0] %= arg1;
        break;
      case 'eql':
        newDimensions[arg0] = (newDimensions[arg0] == arg1) ? 1 : 0;
        break;
      default:
        throw `Invalid operation: ${instruction.operation}`;
    }
  }
  return newDimensions;
}

console.log(`Assigment 1 (mypuzzle): expected: 36969794979199, actual: ${calculate(true, 'mypuzzle.txt')}`);
console.log(`Assigment 2 (mypuzzle): expected: 11419161313147, actual: ${calculate(false, 'mypuzzle.txt')}`);
