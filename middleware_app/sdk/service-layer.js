const {Gateway} = require('fabric-network');
var gatewayfile = require("./gateway/gateway.js")

module.exports.balanceOf = async function balanceOf(username) {
    const gateway = new Gateway();
    let queryResult = await gatewayfile.getBalanceOf(gateway, username);
    return queryResult
} 

module.exports.getTotalSupply = async function totalSupply() {
    const gateway = new Gateway();
    let queryResult = await gatewayfile.getTotalSupply(gateway);
    return queryResult
} 

module.exports.transfer = async function transfer(userTo, userFrom, amount) {
    const gateway = new Gateway();
    let queryResult = await gatewayfile.transfer(gateway, userTo, userFrom, amount);
    return queryResult
} 
