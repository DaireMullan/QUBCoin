process.env.NODE_ENV = 'test';

let chai = require('chai');
let chaiHttp = require('chai-http');
let server = require('../appv2');
let should = chai.should();


chai.use(chaiHttp);

describe('API Test', () => {
    describe('Test totalSupply', () => {
        it('GET /totalSupply', (done) => {
            chai.request(server)
                .get('/totalSupply')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('success');
                    res.body.should.have.property('data');
                    res.body.data.should.have.property('totalSupply');
                    done();
                });
        });
    });

    describe('Test balanceOf', () => {
        it('GET /balanceOf with no username', (done) => {
            chai.request(server)
                .get('/balanceOf/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('GET /balanceOf with wrong username', (done) => {
            chai.request(server)
                .get('/balanceOf/a')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('Username is invalid - Cannot query Balance');
                    done();
                });
        });

        it('GET /balanceOf with centralbank name', (done) => {
            chai.request(server)
                .get('/balanceOf/centralbank@qub.ac.uk')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('success');
                    res.body.should.have.property('data');
                    res.body.data.should.have.property('balance');
                    done();
                });
        });
    });

    describe('Test transfer', () => {
        // transfer will involve changing the state within the blockchain
        // therefore these tests can only verify error handling of the endpoint
        it('POST /transfer with no body', (done) => {
            chai.request(server)
                .post('/transfer')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('QUBCoin transfer parameters are not valid');
                    done();
                });
        });

        it('POST /transfer with no transfer amount', (done) => {
            chai.request(server)
                .post('/transfer')
                .send({userFrom:"centralbank@qub.ac.uk", userTo:"dmullan25@qub.ac.uk"})
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('QUBCoin transfer parameters are not valid');
                    done();
                });
        });

        it('POST /transfer with no user to', (done) => {
            chai.request(server)
                .post('/transfer')
                .send({userFrom:"centralbank@qub.ac.uk", amount:"1"})
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('QUBCoin transfer parameters are not valid');
                    done();
                });
        });

        it('POST /transfer with no user from address', (done) => {
            chai.request(server)
                .post('/transfer')
                .send({amount:"4", userTo:"dmullan25@qub.ac.uk"})
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('QUBCoin transfer parameters are not valid');
                    done();
                });
        });
    });

    describe('Test QR Code', () => {
        it('GET /qr with no text input', (done) => {
            chai.request(server)
                .get('/qr')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('GET /qr with too short text input', (done) => {
            chai.request(server)
                .get('/qr/hi')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    res.body.should.be.a('object');
                    res.body.should.have.property('status');
                    res.body.status.should.be.equal('fail');
                    res.body.should.have.property('data');
                    res.body.data.should.be.equal('QR code text input must be at least 4 characters long');
                    done();
                });
        });

        it('GET /qr with valid text input', (done) => {
            chai.request(server)
                .get('/qr/thisisthetext')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(200);
                    done();
                });
        });
    });


    describe('All other Routes', () => {
        it('Test with post', (done) => {
            chai.request(server)
                .post('/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('Test with put', (done) => {
            chai.request(server)
                .put('/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('Test with delete', (done) => {
            chai.request(server)
                .delete('/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('Test with patch', (done) => {
            chai.request(server)
                .patch('/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });

        it('Test with undefined url', (done) => {
            chai.request(server)
                .post('/undefinedroute/')
                .end((err, res) => {
                    if (err) {
                        done();
                    }
                    res.status.should.equal(404);
                    done();
                });
        });
    });

});
