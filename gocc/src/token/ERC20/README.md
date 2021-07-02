QUBCoin ERC20 token chaincode 
=====
https://theethereum.wiki/w/index.php/ERC20_Token_Standard

Commonly used standard for creating tokens on Ethereum. This implementation of the ERC20 on Fabric is not covering all of the functions. The idea is demonstrate the creation of standard token on Hyeprledger Fabric. For more information please refer to the website link above. To keep things simple this implementation identifies users by "unique id" rather than the public key (in Ethereum)

Functions
=========
- Token created as part of the "chaincode" instantiation
- [totalSupply]    returns the total supply of token in network
- [transfer]    function transfer the specified number of tokens from one user to another
- [balanceOf]   returns the number of tokens owned by the specified user

Install
=======
Install the chaincode on the Acme Peer-1
  .    set-env.sh    acme
  set-chain-env.sh       -n erc20  -v 1.0   -p  token/ERC20   
  chain.sh install -p

Instantiate
===========
Instantiate the chaincode

 set-chain-env.sh        -c   '{"Args":["init","QUBCoin","1000000000", "QUBCoin – Using Blockchain to reward Attendance and other Positive Behaviour","centralbank@qub.ac.uk"]}'
 chain.sh  instantiate

Query
=====
Query the balance for 'centralbank@qub.ac.uk'
 set-chain-env.sh         -q   '{"Args":["balanceOf","centralbank@qub.ac.uk"]}'
 chain.sh query

Invoke
======
Transfer 10 tokens from 'centralbank' to 'dmullan25' a simulation of scanning QR code
  set-chain-env.sh         -i   '{"Args":["transfer", "centralbank@qub.ac.uk", "dmullan25@qub.ac.uk", "10"]}'
  chain.sh  invoke

Query
=====
Check the balance for 'dmullan25@qub.ac.uk' & 'sam'
 set-chain-env.sh         -q   '{"Args":["balanceOf","dmullan25@qub.ac.uk"]}'
 chain.sh query
