const CredentialRequestFactory = require("./credentialRequest/credentialRequestFactory");
const DownloadFailedException = require("./exception/DownloadFailedException");
const CredentialResponseFactory = require("./credentialResponse/CredentialResponseFactory");
const NetworkRequestTimeoutException = require("./exception/NetworkRequestTimeoutException");
const { NETWORK_TIMEOUT, EMPTY_RESPONSE } = require("./constants/constants");
const Logger = require("./common/Logger");
const { default: axios } = require("axios");

/**
 * @param {Object} issuerMetaData 
 * @param {string} proof
 * @param {string} accessToken
 */

async function requestCredential(issuerMetaData, proof, accessToken) {
  const request = CredentialRequestFactory.createCredentialRequest(
    issuerMetaData.credentialFormat,
    accessToken,
    issuerMetaData,
    proof
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
        responseBody
      );
    } else {
      Logger.warn(
        "The response body from credentialEndpoint is empty, returning null"
      );
      return null;
    }
  } catch (error) {
    if (error.code == NETWORK_TIMEOUT) {
      Logger.error(`Network request timeout occured - ${error.message}`);
      throw new NetworkRequestTimeoutException(error.message);
    } else {
      Logger.error(`Error occured while downloading the credential - ${error.message}`);
      throw new DownloadFailedException(error.message);
    }
  }
}

module.exports = {
  requestCredential,
};
