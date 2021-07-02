var express = require("express");
var cors = require('cors')
var bodyParser = require('body-parser')
var serviceLayer = require('../sdk/service-layer');
var jsend = require('jsend');
const QRCode = require('qrcode')
const { PassThrough } = require('stream');

var app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(bodyParser.raw());

app.use(cors())
app.use(jsend.middleware)

const transferErrorString = "Transfer Failure";

/**
 * Initializes the Node express webserver
 */
app.listen(5000, () => {
    console.log("API server available on http://127.0.0.1:5000");
});

/**
 * API endpoint to get the total supply of QUBCOIN in the network
 */
app.get("/totalSupply", (req, res, next) => {
    try {
        serviceLayer.getTotalSupply().then(queryResult => {
            try {
                var totalSupply = JSON.parse(queryResult).response.toString();
                res.jsend.success({ "totalSupply": totalSupply });
            } catch (error) {
                console.log(error);
                res.jsend.error("Total Supply not available.")
            }
        })
    } catch (error) {
        console.log(error);
        res.jsend.error("Total Supply not available.")
    }
});

/**
 * API endpoint to retrieve the QUBCOIN balance for a particular user
 */
app.get("/balanceOf/:username", (req, res, next) => {
    const { username } = req.params;
    if (username == undefined || username == "") {
        res.jsend.fail("No Username supplied - Cannot query Balance");
    } else if (!username.toString().includes("@")) {
        // username is not an email address therefore not valid
        res.jsend.fail("Username is invalid - Cannot query Balance");
    } else {
        serviceLayer.balanceOf(username).then(queryResult => {
            try {
                const userFromQ = JSON.parse(queryResult);
                if ("error" in userFromQ && userFromQ.error == "true") {
                    res.jsend.error(userFromQ.message);
                }
                const usernameresponse = userFromQ.response.owner;
                const balanceresponse = userFromQ.response.balance;
                res.jsend.success(
                    {
                        "username": usernameresponse.toString(),
                        "balance": balanceresponse.toString()
                    });
            } catch (error) {
                console.log(error);
                res.jsend.error("Unable to contact QUBCoin Blockchain")
            }
        })
    }
});

/**
 * API endpoint to transfer a specified value of QUBCOIN between users
 */
app.post("/transfer", (req, res, next) => {
    try {
        const jsonInput = req.body;
        let userFrom = jsonInput.userFrom;
        let userTo = jsonInput.userTo;
        let amount = jsonInput.amount;
        if (userFrom == undefined || userFrom.length == 0 || userTo == undefined || userTo.length == 0 || amount == undefined || amount.length == 0) {
            res.jsend.fail("QUBCoin transfer parameters are not valid");
        } else {
            try {
                serviceLayer.transfer(userTo, userFrom, amount).then(queryResult => {
                    try {
                        let qrjson = JSON.parse(queryResult);
                        if (qrjson.response.includes("Success")) {
                            res.jsend.success("Transfer Success");
                        } else {
                            res.jsend.fail("Could not transfer QUBCoin");
                        }
                    } catch (error) {
                        console.log(error);
                        if ("error" in queryResult && queryResult.error == "true") {
                            res.jsend.error(queryResult.message);
                        } else {
                            res.jsend.error(transferErrorString);
                        }
                    }
                });
            } catch (error) {
                console.log(error)
                res.jsend.error(transferErrorString)
            }
        }
    } catch (error) {
        res.jsend.error(transferErrorString)
    }
});

/**
 * Defines an endpoint for non-app interfaces to retrieve a QUBCoin QR code image
 * Text string must be at least 4 characters long
 */
app.get("/qr/:text", (req, res, next) => {
    const { text } = req.params;
    if (text != undefined && text.toString().length >= 4) {
        try {
            const qrStream = new PassThrough();
            const result = QRCode.toFileStream(qrStream, text,
                {
                    type: 'png',
                    width: 200,
                    errorCorrectionLevel: 'H'
                }
            );

            qrStream.pipe(res);
        } catch (err) { res.jsend.error('Failed to generate QR code: ', err) }
    } else {
        res.jsend.fail("QR code text input must be at least 4 characters long");
    }
});

/**
 * For all other routes, return a 404 HTTP status code
 */
app.use(function (req, res) {
    res.sendStatus(404);
});

module.exports = app;
