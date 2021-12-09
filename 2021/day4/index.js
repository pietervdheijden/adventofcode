const fs = require('fs')

function getDataset()
{
  const data = fs.readFileSync('dataset.txt', 'utf8');
  
  var drawnNumbers = [];
  var boards = [];
  data.split(/\r?\n\n/).forEach(function(block) {
    if (block.includes(',')) {
      drawnNumbers = block.split(',');
    } else {
      var board = [];
      block.split(/\r?\n/).forEach(function(line) {
        line = line.trim();
        line = line.replaceAll('  ', ' ')
        var row = line.split(' ');
        board.push(row);
      });
      boards.push(board);
    }
  });

  return {
    drawnNumbers: drawnNumbers,
    boards: boards
  };
}

function assignment1() {
  var dataset = getDataset();

  var drawnNumbers = dataset.drawnNumbers;
  var boards = dataset.boards;

  for (var drawnNumberIndex = 0; drawnNumberIndex < drawnNumbers.length; drawnNumberIndex++) {
    var drawnNumber = drawnNumbers[drawnNumberIndex];
    
    // Mark number on each board
    boards.forEach((board) => {
      board.forEach((row) => {
        row.forEach(function(number, index, array) {
          if (number == drawnNumber) {
            array[index] = -1;
          }
        });
      });
    });

    // Check if any of the boards has bingo
    for (var boardIndex = 0; boardIndex < boards.length; boardIndex++) {
      var board = boards[boardIndex];
      console.log(board);
      // Check if row is complete
      for (var rowIndex = 0; rowIndex < board.length; rowIndex++) {
        var row = board[rowIndex];
        if (row.filter((value) => value != -1).length == 0) {
          console.log('Bingo!');
          console.log(board);
          console.log(`drawnNumber: ${drawnNumber}`)

          var sumOfUnmarkedNumbers = 0;
          board.forEach((row) => {
            row.forEach((value) => {
              if (value != -1) {
                sumOfUnmarkedNumbers += parseInt(value);
              }
            })
          })
          console.log(`sumOfUnmarkedNumbers: ${sumOfUnmarkedNumbers}`)

          var finalScore = sumOfUnmarkedNumbers * drawnNumber;
          console.log(`finalScore: ${finalScore}`)

          return;
        }
      }

      // Check if column is complete
      for (var columnIndex = 0; columnIndex < board.length; columnIndex++) {
        var column = [];
        for (var rowIndex = 0; rowIndex < board.length; rowIndex++) {
          column.push(board[columnIndex][rowIndex]);
        }

        if (column.filter((value) => value != -1).length == 0) {
          console.log('Bingo!');
          console.log(board);
          console.log(`drawnNumber: ${drawnNumber}`)

          var sumOfUnmarkedNumbers = 0;
          board.forEach((row) => {
            row.forEach((value) => {
              if (value != -1) {
                sumOfUnmarkedNumbers += parseInt(value);
              }
            })
          })
          console.log(`sumOfUnmarkedNumbers: ${sumOfUnmarkedNumbers}`)

          var finalScore = sumOfUnmarkedNumbers * drawnNumber;
          console.log(`finalScore: ${finalScore}`)
        }
      }
    }
  }
}

assignment1()
