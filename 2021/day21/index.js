import { readFileSync } from 'fs';

function calculate(assignment1, dataset, debug = false) {
  const file = `datasets/${dataset}.txt`;
  const data = readFileSync(file, 'utf8');

  // parse dataset
  var player1Position = parseInt(data.split('\n')[0].replace('Player 1 starting position: ',''));
  var player2Position = parseInt(data.split('\n')[1].replace('Player 2 starting position: ',''));

  if (assignment1) { // assignment 1: calculate which player first reaches a score of 1000
    var player1Score = 0;
    var player2Score = 0;
    var nrOfDieRolls = 0;
    var lastDie = 0;
    while (true) {
      // player 1
      var roll = rollDeterministicDie(lastDie) + rollDeterministicDie(lastDie + 1) + rollDeterministicDie(lastDie + 2);
      player1Position = (player1Position + roll) % 10;
      if (player1Position == 0) player1Position = 10;
      player1Score += player1Position;
      lastDie += 3;
      if (lastDie > 100) lastDie -= 100;
      nrOfDieRolls += 3;
      if (player1Score >= 1000) break;

      // player 2
      var roll = rollDeterministicDie(lastDie) + rollDeterministicDie(lastDie + 1) + rollDeterministicDie(lastDie + 2);
      player2Position = (player2Position + roll) % 10;
      if (player2Position == 0) player2Position = 10;
      player2Score += player2Position;
      lastDie += 3;
      if (lastDie > 100) lastDie -= 100;
      nrOfDieRolls += 3;
      if (player2Score >= 1000) break;
    }

    if (debug) {
      console.log(`player1: position=${player1Position}, score=${player1Score}`);
      console.log(`player2: position=${player2Position}, score=${player2Score}`);
      console.log(`nrOfDieRolls: ${nrOfDieRolls}`);
    }

    return Math.min(player1Score, player2Score) * nrOfDieRolls;
  } else { // assignment 2: calculate which player wins in the most universes by reaching a score of at least 21
    var wins = playDiracDie(player1Position, player2Position, 0, 0);
    return Math.max(...wins.map(w => w.wins));
  }  
}

function rollDeterministicDie(lastDie) {
  var die = (lastDie + 1) % 100;
  if (die == 0) die = 100;
  return die;
}

function playDiracDie(player1Position, player2Position, player1Score, player2Score) {
  // calculate how often each roll occurs (1+2+3 == 1+3+2)
  // this reduces the number of recursive calls
  // for example, instead of calculating both 1+2+3 and 1+3+2,
  // only 1+2+3 has to be calculated and the wins can be doubled
  var countPerRoll = {};
  for (var i = 1; i <= 3; i++) {
    for (var j = 1; j <= 3; j++) {
      for (var k = 1; k <= 3; k++) {
        var roll = i + j + k;
        countPerRoll[roll] = (countPerRoll[roll] ?? 0) + 1;
      }
    }
  }

  var wins = [];
  var rolls = Object.keys(countPerRoll).map(roll => parseInt(roll));
  for (var player1Roll of rolls) {
    // player 1
    var player1PositionTmp = (player1Position + player1Roll) % 10;
    if (player1PositionTmp == 0) player1PositionTmp = 10;
    var player1ScoreTmp = player1Score + player1PositionTmp;
    if (player1ScoreTmp >= 21) {
      wins.push({player: 1, wins: 1 * countPerRoll[player1Roll]});
      continue;
    }

    for (var player2Roll of rolls) {
      // player 2
      var player2PositionTmp = (player2Position + player2Roll) % 10;
      if (player2PositionTmp == 0) player2PositionTmp = 10;
      var player2ScoreTmp = player2Score + player2PositionTmp;
      if (player2ScoreTmp >= 21) {
        wins.push({player: 2, wins: 1 * countPerRoll[player1Roll] * countPerRoll[player2Roll]});
        continue;
      }
      
      // game not finished, recursively play (/roll) until either player 1 or 2 wins
      var winsTmp = playDiracDie(
        player1PositionTmp,
        player2PositionTmp,
        player1ScoreTmp,
        player2ScoreTmp
      );
      wins.push(...winsTmp.map(function(w) {
        return {
          player: w.player,
          wins: w.wins * countPerRoll[player1Roll] * countPerRoll[player2Roll]
        }
      }));
    }
  }

  // merge wins and return result
  var mergedWins = [];
  mergedWins.push({player: 1, wins: wins.filter(w => w.player == 1).map(w => w.wins).reduce((a,b) => a+b, 0)});
  mergedWins.push({player: 2, wins: wins.filter(w => w.player == 2).map(w => w.wins).reduce((a,b) => a+b, 0)});
  return mergedWins;
}

console.log(`Assigment 1 (example): expected: 739785, actual: ${calculate(true, 'example')}`);
console.log(`Assigment 1 (mypuzzle): expected: 1002474, actual: ${calculate(true, 'mypuzzle')}`);  
console.log(`Assigment 2 (example): expected: 444356092776315, actual: ${calculate(false, 'example')}`);
console.log(`Assigment 2 (mypuzzle): expected: 919758187195363, actual: ${calculate(false, 'mypuzzle')}`);
