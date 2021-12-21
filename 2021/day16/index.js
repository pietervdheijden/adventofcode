import { readFileSync } from 'fs';

function calculate(assignment1, dataset) {
  const file = `datasets/${dataset}.txt`;
  const hexidecimalData = readFileSync(file, 'utf8');

  var binaryData = '';
  for (var hexChar of hexidecimalData) {
    var binChars = parseInt(hexChar, 16).toString(2);
    while (binChars.length % 4 != 0) binChars = '0' + binChars;
    binaryData += binChars;
  }
  var packets = getPackets(binaryData, 0, -1);
  return assignment1
    ? packets.map(p => p.packetVersion).reduce((a,b) => a + b + 0, 0) // assignment 1
    : packets.filter(p => p.depth == 0)[0].value;                     // assignment 2
}

function getPackets(binaryData, depth, maxPackets) {
  var packets = [];
  
  // min binaryData.length per type:
  // - literalValue: 3+3+5=11
  // - lengthType0 : 3+3+15=21
  // - lengthType1 : 3+3+11=17
  // so exit while loop when binaryData.length < 11
  while (binaryData.length > 10) {
    // exit while loop when the number of subpackets equals maxPackets
    if (maxPackets == packets.filter(p => p.depth == depth).length) break;

    var packetVersion = parseInt(binaryData.substring(0, 3), 2);
    var packetTypeId = parseInt(binaryData.substring(3, 6), 2);
    var value = 0;
    if (packetTypeId == 4) { // literal value
      var index = 6;
      var literalValue = '';
      while (true) {
        var group = binaryData.substring(index, index + 5);
        var prefix = group[0];
        literalValue += group.substring(1);
        index += 5;
        if (prefix == '0') {
          break;
        }
        if (index > binaryData.length) {
          throw `ERROR - Invalid packet: ${binaryData}.`
        }
      }
      var packet = binaryData.substring(0, index);
      value = parseInt(literalValue, 2);
      binaryData = binaryData.substring(index);
    } else { // operator value
      var lengthTypeId = binaryData.substring(6, 7);
      var packet = '';
      var subpackets = [];
      if (lengthTypeId == 0) { 
        // if the length type ID is 0, then the next 15 bits are a number that represents
        // the total length in bits of the sub-packets contained by this packet.
        var subpacketsLength = parseInt(binaryData.substring(7, 22),2);
        var subpacketsBinaryData = binaryData.substring(22, 22 + subpacketsLength);
        subpackets = getPackets(subpacketsBinaryData, depth+1, -1);
        packet = binaryData.substring(0, 22);
      } else if (lengthTypeId == 1) {
        // if the length type ID is 1, then the next 11 bits are a number 
        // that represents the number of sub-packets immediately contained by this packet.
        var subpacketsCount = parseInt(binaryData.substring(7, 18), 2);
        var subpacketIndex = 18;
        subpackets = getPackets(binaryData.substring(subpacketIndex), depth+1, subpacketsCount);
        packet = binaryData.substring(0, 18);
      } else {
        throw `ERROR - Unsupported lengthTypeId: ${lengthTypeId}`;
      }
      // append subpackets to packet
      for (var subpacket of subpackets.filter(sp => sp.depth == depth+1)) {
        packet += subpacket.packet;
      }
      // remove packet from binaryData
      binaryData = binaryData.substring(packet.length);
      // calculate value
      var subpacketValues = subpackets.filter(sp => sp.depth == depth+1).map(s => s.value);
      if (packetTypeId == 0) value = subpacketValues.reduce((a,b) => a+b+0,0);
      if (packetTypeId == 1) value = subpacketValues.reduce((a,b) => a*b);
      if (packetTypeId == 2) value = Math.min(...subpacketValues);
      if (packetTypeId == 3) value = Math.max(...subpacketValues);
      if (packetTypeId == 5) value = subpacketValues[0] > subpacketValues[1] ? 1 : 0;
      if (packetTypeId == 6) value = subpacketValues[0] < subpacketValues[1] ? 1 : 0;
      if (packetTypeId == 7) value = subpacketValues[0] == subpacketValues[1] ? 1 : 0;
      // add subpackets to packets
      packets = packets.concat(...subpackets);
    }
    // add current packet to packets
    packets.push({ depth: depth, packet: packet, packetVersion: packetVersion, packetTypeId: packetTypeId, value: value });
  }
  return packets;
}

console.log(`Assigment 1 (example1.1): expected: 6, actual: ${calculate(true, 'example1.1')}`);
console.log(`Assigment 1 (example1.2): expected: 9, actual: ${calculate(true, 'example1.2')}`);
console.log(`Assigment 1 (example1.3): expected: 14, actual: ${calculate(true, 'example1.3')}`);
console.log(`Assigment 1 (example1.4): expected: 16, actual: ${calculate(true, 'example1.4')}`);
console.log(`Assigment 1 (example1.5): expected: 12, actual: ${calculate(true, 'example1.5')}`);
console.log(`Assigment 1 (example1.6): expected: 23, actual: ${calculate(true, 'example1.6')}`);
console.log(`Assigment 1 (example1.7): expected: 31, actual: ${calculate(true, 'example1.7')}`);
console.log(`Assigment 1 (mypuzzle): expected: 860, actual: ${calculate(true, 'mypuzzle')}`);
console.log(`Assigment 2 (example2.1): expected: 3, actual: ${calculate(false, 'example2.1')}`);
console.log(`Assigment 2 (example2.2): expected: 54, actual: ${calculate(false, 'example2.2')}`);
console.log(`Assigment 2 (example2.3): expected: 7, actual: ${calculate(false, 'example2.3')}`);
console.log(`Assigment 2 (example2.4): expected: 9, actual: ${calculate(false, 'example2.4')}`);
console.log(`Assigment 2 (example2.5): expected: 1, actual: ${calculate(false, 'example2.5')}`);
console.log(`Assigment 2 (example2.6): expected: 0, actual: ${calculate(false, 'example2.6')}`);
console.log(`Assigment 2 (example2.7): expected: 0, actual: ${calculate(false, 'example2.7')}`);
console.log(`Assigment 2 (example2.8): expected: 1, actual: ${calculate(false, 'example2.8')}`);
console.log(`Assigment 2 (mypuzzle): expected: 470949537659, actual: ${calculate(false, 'mypuzzle')}`);
