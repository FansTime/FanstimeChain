var BigNumber = require('bignumber.js');
var Tx = require('ftijs-tx');
var Web3 = require('web3');
var web3 = new Web3('http://localhost:8545');

var userDddress = '0x98b5F12266Ee1341c573478E81AcBc812687109b';
var userPassword = '1984w03r08';

var gasPrice = 0, nonce = 0, txString = '';
web3.eth.personal.unlockAccount(userDddress, userPassword, 0).then(function() {
    web3.eth.getGasPrice().then(function(result) {
        gasPrice = new BigNumber(result);
    }).then(function() {
        web3.eth.getTransactionCount(userDddress).then(function(result) {
            nonce = new BigNumber(result);
        }).then(function() {
            var rawTx = {
                nonce: '0x' + nonce.toString(16),
                gasPrice: '0x' + gasPrice.toString(16),
                gasLimit: '0x200000',
                chainID: '0x6F',  // 111
                from: userDddress,
                value: '0x00',
                data: '0x6060604052605f8060106000396000f3606060405260e060020a6000350463c6888fa18114601a575b005b60586004356007810260609081526000907f24abdb5865df5079dcc5ac590ff6f01d5c16edbc5fab4e195d9febd1114503da90602090a15060070290565b5060206060f3'
            }
            web3.eth.sendTransaction(rawTx, function(err, address) {
                if (!err) console.log("Contract address: " + address);
                else console.log("Failed to create contract: " + err);
            });
        });
    });
});
