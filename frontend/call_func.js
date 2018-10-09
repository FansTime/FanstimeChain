var BigNumber = require('bignumber.js');
var Tx = require('ftijs-tx');
var Web3 = require('web3');
var web3 = new Web3('http://localhost:8545');

var contractAddress = '0x491D389C181eD506E45bFb347408b49fd0A01CDa';
var userAddress = '0x98b5F12266Ee1341c573478E81AcBc812687109b';
var userPassword = '1984w03r08';

var javaCode = 'test1(new byte[]{1,2,3,4,5});', javaHexData = '';
for (i = 0; i < javaCode.length; i++) {
    var hex = javaCode.charCodeAt(i).toString(16);
    javaHexData += ("0" + hex).slice(-2);
}

var gasPrice = 0, nonce = 0, txString = '';
web3.eth.personal.unlockAccount(userAddress, userPassword, 0).then(function() {
    web3.eth.getGasPrice().then(function(result) {
        gasPrice = new BigNumber(result);
    }).then(function() {
        web3.eth.getTransactionCount(userAddress).then(function(result) {
            nonce = new BigNumber(result);
        }).then(function() {
            var rawTx = {
                nonce: '0x' + nonce.toString(16),
                gasPrice: '0x' + gasPrice.toString(16),
                gas: '0x200000',
                chainID: '0x6F',  // 111
                from: userAddress,
                to: contractAddress,
                lang: 'java',
                value: '0x00',
                data: '0x' + javaHexData
            }
            web3.eth.sendTransaction(rawTx, function(err, hash) {
                if (err) console.log("Failed to call contract: " + err);
                web3.eth.getTransactionReceipt(hash, function(err, receipt) {
                    if (err) console.log("Failed to get receipt: " + err);
                    else {
                        //console.log(receipt);
                        console.log("CompileError: " + receipt['error']);
                        
                        var jsonLogs = receipt['logs'];
                        for (var i = 0; i < jsonLogs.length; ++i) {
                            var rawStr = jsonLogs[i]['data'].substr(0,2).toLowerCase() === "0x"
                                       ? jsonLogs[i]['data'].substr(2) : jsonLogs[i]['data'];
                            var resultStr = [];
                            for (var j = 0; j < rawStr.length; j += 2) {
                                var ch = parseInt(rawStr.substr(j, 2), 16);
                                resultStr.push(String.fromCharCode(ch));
                            }
                            console.log("UserLog-" + i + ": " + resultStr.join(""));
                        }
                    }
                    
                    // FIXME: correct result may be read next time
                    web3.eth.getStorageAt(contractAddress, 10).then(function(value) {
                        console.log("Storage-10: " + value);
                    });
                });
            });
        });
    });
});
