const LdpVcCredentialRequest = require("./types/LdpVcCredentialRequest");
const {CredentialFormat} = require("../constants/CredentialFormat");
const Logger = require("../common/Logger");
const DownloadFailedException = require("../exception/DownloadFailedException");

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
        const errorMessage = `Unsupported credential format type: ${credentialFormat}`;
        Logger.error(errorMessage);
        throw new DownloadFailedException(errorMessage);
    }
  }
}

module.exports = CredentialRequestFactory; 