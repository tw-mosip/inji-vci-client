class CredentialRequest {
    constructor(accessToken, issuerMetaData, proof) {
        this.accessToken = accessToken;
        this.issuerMetaData = issuerMetaData;
        this.proof = proof;
    }

    constructRequest(){}
}

module.exports = CredentialRequest;