const LdpVcCredentialRequest = require("./types/LdpVcCredentialRequest");
const {CredentialFormat} = require("../constants/CredentialFormat");

class CredentialRequestFactory {
   static createCredentialRequest(
    credentialFormat,
    accessToken,
    issuerMetaData,
    proof
  ) {
    switch (credentialFormat) {
      case CredentialFormat.LDP_VC:
        return new LdpVcCredentialRequest(accessToken, issuerMetaData, proof).constructRequest();
      default:
        throw new Error("Unsupported credential format");
    }
  }
}

module.exports = CredentialRequestFactory; 