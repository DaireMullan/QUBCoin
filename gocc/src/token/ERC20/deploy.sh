#!/bin/bash

echo    "Installing the QUBCoin chaincode ERC20"
.    set-env.sh    acme
set-chain-env.sh       -n erc20  -v 1.0   -p  token/ERC20   
chain.sh install -p

echo    "Instantiating Chaincode"
set-chain-env.sh        -c   '{"Args":["init","QUBCoin","1000000000", "QUBCoin – Using Blockchain to reward Attendance and other Positive Behaviour","centralbank@qub.ac.uk"]}'
chain.sh  instantiate

echo "Done."