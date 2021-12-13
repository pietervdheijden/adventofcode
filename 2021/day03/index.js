const lineReader = require('line-reader');

function assignment1() {
  var numberOfBits = 12;
  zeroCounts = [...Array(numberOfBits)].map(x => 0);
  oneCounts = [...Array(numberOfBits)].map(x => 0);
  gammaRateArray = [...Array(numberOfBits)].map(x => 0);
  epsilonRateArray = [...Array(numberOfBits)].map(x => 0);

  lineReader.eachLine('dataset.txt', function(line, last) {
      for (let i = 0; i < numberOfBits; i++) {
        bit = line[i];
        if (bit == 0) {
          zeroCounts[i] = zeroCounts[i] + 1;
        } else if (bit == 1) {
          oneCounts[i]++;
        }
        else {
          throw `Unsupported bit: ${bit}`;
        }
      }

      if (last) {
        for (let i = 0; i < numberOfBits; i++) {
          if (zeroCounts[i] > oneCounts[i]) {
            gammaRateArray[i] = 0;
            epsilonRateArray[i] = 1;
          } else if (zeroCounts[0] < oneCounts[1]) {
            gammaRateArray[i] = 1;
            epsilonRateArray[i] = 0;
          } else {
            throw `ERROR: zeroCounts and oneCounts cannot be equal! bit=${i}.`
          }
        }

        gammaRateBit = gammaRateArray.join("");
        epsilonRateBit = epsilonRateArray.join("");

        gammaRate = parseInt(gammaRateBit, 2)
        epsilonRate = parseInt(epsilonRateBit, 2)

        answer = gammaRate * epsilonRate;

        console.log("Assignment 1:");
        console.log(`- gammaRateBit: ${gammaRateBit}`);
        console.log(`- epsilonRateBit: ${epsilonRateBit}`);
        console.log(`- gammaRate: ${gammaRate}`);
        console.log(`- epsilonRate: ${epsilonRate}`);
        console.log(`- answer: ${answer}`);
      }
  });
}

function assignment2() {
  var rowNumber = 0;
  var rows = [];
  lineReader.eachLine('dataset.txt', function(line, last) {
    rows.push({rowNumber: rowNumber, binaryNumber: line})
    rowNumber++;

    if (last) {
      var oxygenGeneratorRating = CalculateOxygenGeneratorRating(rows);
      var co2ScrubberRating = CalculateCo2ScrubberRating(rows)
      var lifeSupportRating = oxygenGeneratorRating * co2ScrubberRating;

      console.log("Assignment 2:");
      console.log(`- oxygenGeneratorRating: ${oxygenGeneratorRating}`);
      console.log(`- co2ScrubberRating: ${co2ScrubberRating}`);
      console.log(`- lifeSupportRating: ${lifeSupportRating}`);
    }
  });
}

function CalculateOxygenGeneratorRating(rows) {
  var numberOfBits = rows[0].binaryNumber.length;
  for (let position = 0; position < numberOfBits; position++) {
    var zeroRows = [];
    var oneRows = [];
    
    rows.forEach(row => {
      var bit = row.binaryNumber[position];
      if (bit == 0) {
        zeroRows.push(row);
      } else if (bit == 1) {
        oneRows.push(row);
      } else {
        throw `Unsupported bit: ${bit}`;
      }
    })

    if (oneRows.length >= zeroRows.length) {
      rows = oneRows;
    } else {
      rows = zeroRows;
    }

    // console.log(`Position: ${position}, Number of rows left: ${rows.length}`);

    if (rows.length == 1) {
      var oxygenGeneratorRatingBinary = rows[0].binaryNumber;
      oxygenGeneratorRating = parseInt(oxygenGeneratorRatingBinary, 2);
      return oxygenGeneratorRating;
    }
  }
}

function CalculateCo2ScrubberRating(rows, numberOfBits) {
  var numberOfBits = rows[0].binaryNumber.length;
  for (let position = 0; position < numberOfBits; position++) {
    var zeroRows = [];
    var oneRows = [];
    
    rows.forEach(row => {
      var bit = row.binaryNumber[position];
      if (bit == 0) {
        zeroRows.push(row);
      } else if (bit == 1) {
        oneRows.push(row);
      } else {
        throw `Unsupported bit: ${bit}`;
      }
    })

    if (oneRows.length >= zeroRows.length) {
      rows = zeroRows;
    } else {
      rows = oneRows;
    }

    if (rows.length == 1) {
      var co2ScrubberRatingBinary = rows[0].binaryNumber;
      var co2ScrubberRating = parseInt(co2ScrubberRatingBinary, 2);

      return co2ScrubberRating;
    }
  }
}

assignment1();
assignment2();