# Execute this script within VM to tear down all instances and then to setup QUBCoin ERC20 token chaincode on blockchain 
sanitize.sh 

dev-init.sh 

source set-env.sh acme

set-chain-env.sh -n erc20 -v 1.0 -p token/erc20 -c '{"Args":["init","QUBCoin","1000000000", "QUBCoin – Using Blockchain to reward Attendance and other Positive Behaviour","centralbank@qub.ac.uk"]}'

chain.sh install -p

chain.sh instantiate 
