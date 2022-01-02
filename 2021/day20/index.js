import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var enhancementAlgorithm = data.split('\n\n')[0].split('').map(c => c == '#' ? 1 : 0);
  var image = data.split('\n\n')[1].split('\n').map(row => row.split('').map(c => c == '#' ? 1 : 0));
  
  // enhance image
  var enhancementCount = assignment1 ? 2 : 50;
  for (var enhancementIteration = 1; enhancementIteration <= enhancementCount; enhancementIteration++) {
    var newImage = [];
    for (var i = -1; i < image.length + 1; i++) {
      newImage.push([]);
      for (var j = -1; j < image[0].length + 1; j++) {
        var binaryValue = '';
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i-1,j-1); // left top
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i-1,j);   // top
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i-1,j+1); // right top
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i,  j-1); // left
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i,  j);   // middle
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i,  j+1); // right
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i+1,j-1); // left bottom
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i+1,j);   // bottom
        binaryValue += getPixelValue(image, enhancementAlgorithm, enhancementIteration, i+1,j+1); // right bottom

        var intValue = parseInt(binaryValue, 2);
        var algorithmValue = enhancementAlgorithm[intValue];
        newImage[i+1].push(algorithmValue);
      }
    }
    image = newImage;
  }

  // print debug messages
  if (debug) {
    console.log('image:')
    for (var pixels of image) {
      console.log(pixels.map(pixel => pixel == 1 ? '#' : '.').join(''))
    }
  }

  // return number of lit pixels (i.e. pixels with value == 1)
  return [].concat.apply([], image).filter(pixel => pixel == 1).length;
}

function getPixelValue(image, enhancementAlgorithm, enhancementIteration, row, column) {
  // pixel is outside the image, therefore calculate the pixel value
  if (row < 0 || column < 0 || image.length-1 < row || image[row].length-1 < column) {
    var firstValueAlgorithm = enhancementAlgorithm[0];
    var lastValueAlgorithm = enhancementAlgorithm[enhancementAlgorithm.length-1];
    if (firstValueAlgorithm == 1 && lastValueAlgorithm == 1)
      throw `Invalid enhancement algorithm which causes infinite lit pixels!`
    
    // if the first value == 0, then the outside pixels will not be lit, so return 0
    if (firstValueAlgorithm == 0) return 0;

    // first value == 1 && last value == 0, then the outside pixels will flip every iteration
    return enhancementIteration % 2 == 0 ? firstValueAlgorithm : lastValueAlgorithm;
  }

  // pixel is inside the image, so "just" return the pixel value.
  return image[row][column];
}

console.log(`Assigment 1 (example): expected: 35, actual: ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): expected: 5301, actual: ${calculate(true, 'mypuzzle')}`);  
console.log(`Assigment 2 (example): expected: 3351, actual: ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): expected: 19492, actual: ${calculate(false, 'mypuzzle')}`);
