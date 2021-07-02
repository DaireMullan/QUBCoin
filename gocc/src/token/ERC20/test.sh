#!/bin/bash

# Modified to test QUBCoin implementation

# Include the Chaincode environment properties
source cc.env.sh

# Include the unit test driver
source  utest.sh

CC_PATH=token/ERC20
CC_NAME=erc20
CC_VERSION=1.0
CC_CHANNEL_ID=airlinechannel

# Setup the logging level for peer binary
# export CORE_LOGGING_LEVEL='INFO'
export FABRIC_LOGGING_SPEC='ERROR'

# If you would like to generate a unique CC_NAME everytime
# DO NOT USE THIS in 'dev' mode
CC_ORIGINAL_NAME=$CC_NAME
generate_unique_cc_name
set-chain-env.sh -n $CC_NAME

# Set the Organization Context to acme
set_org_context  acme

# Install
chain_install 

# Instantiate
CC_CONSTRUCTOR='{"Args":["init","QUBCoin","1000000000", "QUBCoin – Using Blockchain to reward Attendance and other Positive Behaviour","centralbank@qub.ac.uk"]}'
chain_instantiate
# if needed sleep for additinal time in sec using e.g.,    txn_sleep   3s

echo "---- VALID TEST CASES ----"

# Test INIT
set_test_case   'Chaincode Should be initialized with "centralbank@qub.ac.uk" as owner of 1 billion tokens'
export CC_QUERY_ARGS='{"Args":["balanceOf","centralbank@qub.ac.uk"]}'
chain_query 
assert_json_equal "$QUERY_RESULT" '.response.balance' "1000000000"

#Test TOTALSUPPLY
set_test_case   'ERC20 token should have total supply equal to 1 billion tokens'
export CC_QUERY_ARGS='{"Args":["totalSupply"]}'
chain_query 
assert_json_equal "$QUERY_RESULT" '.response' "1000000000"

## Test TRANSFER and BALANCE OF
set_test_case   'Transfer 10 token from centralbank to dmullan25 - balance for centralbank should now be 999999990'
# Invoke the transfer
export CC_INVOKE_ARGS='{"Args":["transfer", "centralbank@qub.ac.uk", "dmullan25@qub.ac.uk", "10"]}'
chain_invoke 

# Test BALANCE OF CENTRALBANK after TRANSFER 
CC_QUERY_ARGS='{"Args":["balanceOf","centralbank@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "999999990"

# Test BALANCE OF DMULLAN25 after TRANSFER 
set_test_case   'dmullan25 balance should be 10'
CC_QUERY_ARGS='{"Args":["balanceOf","dmullan25@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "10"

# Test BALANCE OF JOEBLOGGS  
set_test_case   'joebloggs balance should be 0'
CC_QUERY_ARGS='{"Args":["balanceOf","joebloggs@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "0"

# Test TOTALSUPPLY again to ensure no difference
set_test_case   'ERC20 token should not change and have total supply equal to 1 billion tokens'
export CC_QUERY_ARGS='{"Args":["totalSupply"]}'
chain_query 
assert_json_equal "$QUERY_RESULT" '.response' "1000000000"

echo "---- INVALID / ERROR TEST CASES ----"

# Test TRANSFER from user with zero balance
set_test_case   'Transfer 10 token from userwithzerobalance to dmullan25 - should error  and dmullan25 not gain QUBCoin'
# Invoke the transfer
export CC_INVOKE_ARGS='{"Args":["transfer", "userwithzerobalance@qub.ac.uk", "dmullan25@qub.ac.uk", "10"]}'
chain_invoke
CC_QUERY_ARGS='{"Args":["balanceOf","dmullan25@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "10"

# Test TRANSFER from with negative amount
set_test_case   'Transfer NEGATIVE VALUE -10 QUBCoin token from centralbank to dmullan25 - should error and both users QUBCoin value remain unchanged'
# Invoke the transfer
export CC_INVOKE_ARGS='{"Args":["transfer", "centralbank@qub.ac.uk", "dmullan25@qub.ac.uk", "-10"]}'
chain_invoke
CC_QUERY_ARGS='{"Args":["balanceOf","dmullan25@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "10"
CC_QUERY_ARGS='{"Args":["balanceOf","centralbank@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "999999990"

# Test TRANSFER from with not enough balance for transfer
set_test_case   'Transfer LARGE VALUE 2500 QUBCoin token from centralbank to dmullan25 - should error and both users QUBCoin value remain unchanged'
# Invoke the transfer
export CC_INVOKE_ARGS='{"Args":["transfer", "dmullan25@qub.ac.uk", "joebloggs@qub.ac.uk", "2500"]}'
chain_invoke
CC_QUERY_ARGS='{"Args":["balanceOf","dmullan25@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "10"
CC_QUERY_ARGS='{"Args":["balanceOf","joebloggs@qub.ac.uk"]}' 
chain_query
assert_json_equal "$QUERY_RESULT"  '.response.balance'  "0"

