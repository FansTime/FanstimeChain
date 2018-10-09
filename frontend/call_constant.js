var BigNumber = require('bignumber.js');
var Tx = require('ftijs-tx');
var Web3 = require('web3');
var web3 = new Web3('http://localhost:8545');

var contractAddress = '0xcebfad6b07885a7bf084f65d5c0f1f773511adc9';
var userAddress = '0x98b5F12266Ee1341c573478E81AcBc812687109b';

var javaCode = '//!java\n'  // FIXME: must have this head?...
             + 'testR(new byte[]{0x98,0xb5,0xF1,0x22,0x66,0xEe,0x13,0x41,0xc5,0x73,0x47,0x8E,0x81,0xAc,0xBc,0x81,0x26,0x87,0x10,0x9b});',
    javaHexData = '';
for (i = 0; i < javaCode.length; i++) {
    var hex = javaCode.charCodeAt(i).toString(16);
    javaHexData += ("0" + hex).slice(-2);
}

web3.eth.call({
    from: userAddress,
    to: contractAddress,
    data: '0x' + javaHexData
}).then(function(result) {
    console.log(result);
});
