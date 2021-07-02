const fs = require('fs');
const yaml = require('js-yaml');
// Import gateway class
const { Gateway, FileSystemWallet, DefaultEventHandlerStrategies, Transaction } = require('fabric-network');

// Constants for profile
const CONNECTION_PROFILE_PATH = '../profiles/dev-connection.yaml'
// Path to the wallet
const FILESYSTEM_WALLET_PATH = './user-wallet'
// Identity context used
const USER_ID = 'Admin@acme.com'
// Channel name
const NETWORK_NAME = 'airlinechannel'
// Chaincode
const CONTRACT_ID = "erc20"

/**
 * Executes the functions for query & invoke
 */
module.exports.getBalanceOf = async function getBalanceOf(gateway, username) {
    try {
        gateway = await setupGateway(gateway)
        let network = await gateway.getNetwork(NETWORK_NAME)
        const contract = await network.getContract(CONTRACT_ID);

        var balanceResult = await queryBalanceOf(contract, username)
        return balanceResult
    } catch (e) {
        return { "error": "true", "message": "Could not query Blockchain for balance, Gateway error" }.toString();
    }
}

module.exports.getTotalSupply = async function getTotalSupply(gateway) {
    try {
        gateway = await setupGateway(gateway)
        let network = await gateway.getNetwork(NETWORK_NAME)
        const contract = await network.getContract(CONTRACT_ID);

        var totalSupply = await queryTotalSupply(contract)
        return totalSupply
    } catch (e) {
        return { "error": "true", "message": "Could not query Blockchain for totalSupply, Gateway error" }.toString();
    }
}

module.exports.transfer = async function transfer(gateway, userTo, userFrom, amount) {
    try {
        gateway = await setupGateway(gateway)
        let network = await gateway.getNetwork(NETWORK_NAME)
        const contract = await network.getContract(CONTRACT_ID);

        let transactionStatus = await submitTxnContract(contract, userFrom, userTo, amount)
        return transactionStatus
    } catch (e) {
        return { "error": "true", "message": "Could not make Blockchain transfer, Gateway error" };
    }
}

/**
 * Queries the chaincode for the Balance for a given user
 * @param {object} contract 
 * @param {string} username
 */
async function queryBalanceOf(contract, username) {
    try {
        let response = await contract.evaluateTransaction('balanceOf', username)
        return response.toString()
    } catch (e) {
        console.log(e)
        return { "error": "true", "message": "Could not query Blockchain for balance" }.toString();
    }
}

/**
 * Queries the chaincode for the Total supply in the network
 * @param {object} contract 
 */
async function queryTotalSupply(contract) {
    try {
        let response = await contract.evaluateTransaction('totalSupply')
        return response.toString()
    } catch (e) {
        console.log(e)
        return { "error": "true", "message": "Could not query Blockchain for totalSupply" }.toString();
    }
}

/**
 * Submit the transaction
 * @param {object} contract 
 * @param {string} userTo 
 * @param {string} userFrom 
 * @param {string} amount 
 */
async function submitTxnContract(contract, userTo, userFrom, amount) {
    try {
        // Submit the transaction
        let response = await contract.submitTransaction('transfer', userTo, userFrom, amount)
        return response.toString()
    } catch (e) {
        console.log(e)
        try {
            let errString = JSON.parse(e.responses[0].message)["error"].toString();
            return { "error": "true", "message": errString };
        } catch (e) {
            return { "error": "true", "message": "Could not make Blockchain transfer\nTransfer Amount must be positive integer" };
        }
    }
}

/**
 * Function for setting up the gateway
 * It does not actually connect to any peer/orderer
 */
async function setupGateway(gateway) {

    // load the connection profile into a JS object
    let connectionProfile = yaml.safeLoad(fs.readFileSync("../sdk/profiles/dev-connection.yaml", 'utf8'));

    // Need to setup the user credentials from wallet
    const wallet = new FileSystemWallet("../sdk/gateway/user-wallet")

    // Set up the connection options
    let connectionOptions = {
        identity: USER_ID,
        wallet: wallet,
        discovery: { enabled: false, asLocalhost: true }
        , eventHandlerOptions: {
            strategy: null
        }
    }

    // Connect gateway to the network
    await gateway.connect(connectionProfile, connectionOptions)
    return gateway
}
