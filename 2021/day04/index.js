const fs = require('fs')

function getDataset()
{
  const data = fs.readFileSync('dataset.txt', 'utf8');
  // const data = fs.readFileSync('dataset-example.txt', 'utf8');
  
  var drawnNumbers = [];
  var boards = [];
  data.split(/\r?\n\n/).forEach(function(block) {
    if (block.includes(',')) {
      drawnNumbers = block.split(',').map(n => parseInt(n));
    } else {
      var board = [];
      block.split(/\r?\n/).forEach(function(line) {
        line = line.trim();
        line = line.replaceAll('  ', ' ')
        var row = line.split(' ').map(n => parseInt(n));
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

// get first bingos
function assignment1() {
  var dataset = getDataset();

  var drawnNumbers = dataset.drawnNumbers;
  var boards = dataset.boards;

  var bingos = play(drawnNumbers, boards);
  if (!bingos) {
    throw 'No bingo could be found!'
  }
  
  console.log('\nAssignment 1:');
  console.log('- First bingos: ');
  bingos.forEach(bingo => { 
    console.log(bingo)
  });
}

// get last bingos
function assignment2() {
  var dataset = getDataset();

  var drawnNumbers = dataset.drawnNumbers;
  var boards = dataset.boards;

  var bingos = []
  while (boards.length > 0 && drawnNumbers.length != 0) {
    bingos = play(drawnNumbers, boards);
    if (!bingos) {
      throw 'No bingo could be found!'
    }

    drawnNumbers = drawnNumbers.slice(bingos[0].drawnNumberIndex+1, drawnNumbers.length);
    bingos.forEach(bingo => {
      boards = boards.filter(b => b != bingo.board)
    });
  }

  console.log('\nAssignment 2:');
  console.log('- Last bingos: ');
  bingos.forEach(bingo => {
    console.log(bingo);
  })
}

function play(drawnNumbers, boards) {
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
    var bingos = [];
    outerLoop:
    for (var boardIndex = 0; boardIndex < boards.length; boardIndex++) {
      var board = boards[boardIndex];
      
      // Check if row is complete
      for (var rowIndex = 0; rowIndex < board.length; rowIndex++) {
        var row = board[rowIndex];
        if (row.filter((value) => value != -1).length == 0) {
          bingos.push(getBingo(drawnNumberIndex, drawnNumber, boardIndex, board));
          continue outerLoop;
        }
      }

      // Check if column is complete
      for (var columnIndex = 0; columnIndex < board.length; columnIndex++) {
        var column = [];
        for (var rowIndex = 0; rowIndex < board.length; rowIndex++) {
          column.push(board[rowIndex][columnIndex]);
        }
        if (column.filter((value) => value != -1).length == 0) {
          bingos.push(getBingo(drawnNumberIndex, drawnNumber, boardIndex, board));
          continue outerLoop;
        }
      }
    }
    if (bingos.length > 0) {
      return bingos;
    }
  }
}

function getBingo(drawnNumberIndex, drawnNumber, boardIndex, board) {
  var sumOfUnmarkedNumbers = 0;
  board.forEach((row) => {
    row.forEach((value) => {
      if (value != -1) {
        sumOfUnmarkedNumbers += value;
      }
    })
  })
  var finalScore = sumOfUnmarkedNumbers * drawnNumber;

  return {
    drawnNumberIndex: drawnNumberIndex,
    drawnNumber: drawnNumber,
    boardIndex: boardIndex,
    board: board,
    sumOfUnmarkedNumbers: sumOfUnmarkedNumbers,
    finalScore
  }
}

assignment1();
assignment2();
