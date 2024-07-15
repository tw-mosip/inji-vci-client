const CredentialRequestFactory = require("./credentialRequest/credentialRequestFactory");
const DownloadFailedException = require("./exception/DownloadFailedException");
const CredentialResponseFactory = require("./credentialResponse/CredentialResponseFactory");
const NetworkRequestTimeoutException = require("./exception/NetworkRequestTimeoutException");
const { NETWORK_TIMEOUT, EMPTY_RESPONSE } = require("./constants/constants");
const Logger = require("./common/Logger");
const { default: axios } = require("axios");
const IssuerMetaData = require('./dto/issuerMetaData');
const Proof = require('./proof/Proof');

class VCIClient {
  constructor(traceId) {
    this.traceId = traceId;
  }

/**
 * @param {IssuerMetaData} issuerMetaData 
 * @param {Proof} proof
 * @param {string} accessToken
 * @returns {Promise<CredentialResponse | null>}
 * @throws {NetworkRequestTimeoutException | DownloadFailedException}
 */

 async requestCredential(issuerMetaData, proof, accessToken) {
  const request = CredentialRequestFactory.createCredentialRequest(
    issuerMetaData.credentialFormat,
    accessToken,
    issuerMetaData,
    proof,
  );

  try {
    const response = await axios.post(
      issuerMetaData.credentialEndpoint,
      request.requestBody,
      {
        timeout: issuerMetaData.downloadTimeoutInMillSeconds,
        headers: request.requestHeader,
      }
    );

    if (response.data !== EMPTY_RESPONSE) {
      const responseBody = JSON.stringify(response.data);
      return CredentialResponseFactory.createCredentialResponse(
        issuerMetaData.credentialFormat,
        responseBody, 
      );
    } else {
      Logger.warn(
        "The response body from credentialEndpoint is empty, returning null", this.traceId
      );
      return null;
    }
  } catch (error) {
    if (error.code == NETWORK_TIMEOUT) {
      Logger.error(`Network request timeout occured - ${error.message}`, this.traceId);
      throw new NetworkRequestTimeoutException(error.message);
    } else {
      Logger.error(`Error occured while downloading the credential - ${error.message}`, this.traceId);
      throw new DownloadFailedException(error.message);
    }
  }
}
}

module.exports = VCIClient;
