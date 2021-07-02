var express = require("express");
var cors = require('cors')
var bodyParser = require('body-parser')
var serviceLayer = require('../sdk/service-layer');
const QRCode = require('qrcode')
const { json } = require("body-parser");
const { PassThrough } = require('stream');

var app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(bodyParser.raw());

app.use(cors())

/**
 * Define standard error responses to return in many cases
 */
const balanceOfErrorResponse = {
    "username": "User does not exist in the ledger!",
    "balance": "0.0"
}

const totalSupplyErrorResponse = {
    "totalSupply": "Total Supply not available."
}

const transferErrorResponse = { 
    "response": "Transfer Failure"
}

/**
 * Initializes the Node express webserver
 */
app.listen(4000, () => {
    console.log("API server available on http://127.0.0.1:4000");
});

/**
 * API endpoint to get the total supply of QUBCOIN in the network
 */
app.get("/totalSupply", (req, res, next) => {
    serviceLayer.getTotalSupply().then(queryResult => {
        try {
            var totalSupplyResponse = JSON.parse(queryResult);
            var totalSupply = totalSupplyResponse.response.toString();
            res.json(
                { "totalSupply": totalSupply });
        } catch (error) {
            res.json(totalSupplyErrorResponse);
        }
    })
});

/**
 * API endpoint to retrieve the QUBCOIN balance for a particular user
 */
app.get("/balanceOf/:username", (req, res, next) => {
    const { username } = req.params;
    if (username !== "") {
        serviceLayer.balanceOf(username).then(queryResult => {
            try {
                const userFromQ = JSON.parse(queryResult);
                const usernameresponse = userFromQ.response.owner;
                const balanceresponse = userFromQ.response.balance;
                res.json(
                    {
                        "username": usernameresponse.toString(),
                        "balance": balanceresponse.toString()
                    });
            } catch (error) { res.json(balanceOfErrorResponse); }
        })
    } else { res.json(balanceOfErrorResponse); }
});

/**
 * API endpoint to transfer a specified value of QUBCOIN between users
 */
app.post("/transfer", (req, res, next) => {
    const jsonInput = req.body;
    let userFrom = jsonInput.userFrom;
    let userTo = jsonInput.userTo;
    let amount = jsonInput.amount;
    if (userFrom !== "" && userTo !== "" && amount !== "") {
        serviceLayer.transfer(userTo, userFrom, amount).then(queryResult => {
            try {
                let qrjson = JSON.parse(queryResult)
                res.json(qrjson);
            } catch (error) {
                console.log(error);
                res.json(transferErrorResponse)
            }
        });
    } else { res.json(transferErrorResponse) }
});

app.get("/qr/:text", (req, res, next) => {
    const { text } = req.params;
    if (text !== "") {
        try{
            const qrStream = new PassThrough();
            const result =  QRCode.toFileStream(qrStream, text,
                        {
                            type: 'png',
                            width: 200,
                            errorCorrectionLevel: 'H'
                        }
                    );

            qrStream.pipe(res);
        } catch(err){
            res.json({"response": 'Failed to generate QR code: ', err})
        }
    } else {
        res.json({"response": "Cannot get QR code with empty text parameter."});
    }
});
