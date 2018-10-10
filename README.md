# FanstimeChain
FansTime Chain is a public chain created by FansTime with the Dpos+Pow algorithm based on the characteristics of fan's economic vertical ecology, aiming at improving the ecological operation efficiency of stars and fans, helping stars to issue token by one click, and realizing the maximization of personal IP value for stars and the meet-up, interactive dream and value return for fans. 
By imbedding feature modules in vertical domain of FansTime public chain, the one-click issuance of personal token will be simplified, and one-click signature, piracy identification of copyright, etc. for protecting works copyrights, realized. At the same time, for preventing the community interest being damaged by very few ill-disposed token issuers, Fanstime public chain will also build in some security functions such as mandatory token locking, mandatory staging release, etc. for protecting fans’ rights and interests.

* Website: https://www.fanstime.org/english.html
* Whitepaper:https://www.fanstime.org/FansTime-en.pdf


# How To
## Start private chain step
1. Go to the project root directory and execute 
` ./gradlew runPrivate
### Add a wallet and assign balance steps
1. Start the private chain
2. run
> Curl -X POST --data'{"jsonrpc": "2.0", "method": "personal_newAccount", "params":["passphrase"], "id":1}'${host}
3. Stop private chain
4. Record the address returned in the previous step and modify the genesis-private.json file to add "youraddress" to the alloc attribute: {"balance": "yourbalance"}
5. Clear the FTI database, the database address is the FTI folder in the user directory.
6. Restart the private chain
