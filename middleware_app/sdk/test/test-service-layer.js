process.env.NODE_ENV = 'test';

let chai = require('chai');
let serviceLayer = require('../service-layer');
let should = chai.should();


describe('Gateway Service Layer Test', () => {
    it('get totalSupply', () => {
        return serviceLayer.getTotalSupply().then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.be.a('number');
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });

    it('get balance of for centralbank', () => {
        const centralbank = "centralbank@qub.ac.uk"
        return serviceLayer.balanceOf(centralbank).then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.be.a('object');
                jsonRes.response.should.have.property("owner");
                jsonRes.response.should.have.property("balance");
                jsonRes.response.owner.should.equal(centralbank);
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });

    it('get balance of for non-participating user', () => {
        const username = "test-username-not-participating"
        return serviceLayer.balanceOf(username).then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.be.a('object');
                jsonRes.response.should.have.property("owner");
                jsonRes.response.should.have.property("balance");
                jsonRes.response.owner.should.equal(username);
                jsonRes.response.balance.should.equal(0);
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });

    it('get balance of empty username', () => {
        const username = ""
        return serviceLayer.balanceOf(username).then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.be.a('object');
                jsonRes.response.should.have.property("owner");
                jsonRes.response.should.have.property("balance");
                jsonRes.response.owner.should.equal(username);
                jsonRes.response.balance.should.equal(0);
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });

    it('successful transfer from centralbank user', () => {
        const centralbank = "centralbank@qub.ac.uk";
        const testuser = "qubcointransfertest@qub.ac.uk";
        const amount = "1";
        return serviceLayer.transfer(testuser, centralbank, amount).then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.equal('Transfer Successful');
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });

    it('error transfer to centralbank user with zero amount', () => {
        const centralbank = "centralbank@qub.ac.uk";
        const testuser = "qubcointransfertest@qub.ac.uk";
        const amount = "0";
        return serviceLayer.transfer(testuser, centralbank, amount).then(res => {
            res.should.have.property("error");
            res.error.should.equal("true"); 
        });
    });

    it('get totalSupply at end of testing', () => {
        return serviceLayer.getTotalSupply().then(res => {
            try {
                let jsonRes = JSON.parse(res);
                jsonRes.should.be.a('object');
                jsonRes.should.have.property("response");
                jsonRes.response.should.be.a('number');
            } catch (e) {
                throw new Error(e);
            }
        }, reason => {
            throw new Error(reason);
        });
    });
});