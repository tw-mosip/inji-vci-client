const Proof = require("../Proof");
const { ProofType } = require("../../constants/ProofType");

class JWTProof extends Proof {
    constructor(jwt) {
        super(ProofType.JWT);
        this.jwt = jwt;
      }
}

module.exports = JWTProof;
