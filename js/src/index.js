const CredentialRequestFactory = require("./credentialRequest/credentialRequestFactory");
const DownloadFailedException = require("./exception/DownloadFailedException");
const CredentialResponseFactory = require("./credentialResponse/CredentialResponseFactory");
const NetworkRequestTimeoutException = require("./exception/NetworkRequestTimeoutException");
const { NETWORK_TIMEOUT, EMPTY_RESPONSE } = require("./constants/constants");
const Logger = require("./common/Logger");
const { default: axios } = require("axios");

/**
 * @param {Object} issuerMeta
 * @param {string} proof
 * @param {string} accessToken
 */

async function requestCredential(issuerMeta, proof, accessToken) {
  const request = CredentialRequestFactory.createCredentialRequest(
    issuerMeta.credentialFormat,
    accessToken,
    issuerMeta,
    proof
  );

  try {
    const response = await axios.post(
      issuerMeta.credentialEndpoint,
      request.requestBody,
      {
        timeout: issuerMeta.downloadTimeoutInMillSeconds,
        headers: request.requestHeader,
      }
    );

    if (response.data !== EMPTY_RESPONSE) {
      const responseBody = JSON.stringify(response.data);
      return CredentialResponseFactory.createCredentialResponse(
        issuerMeta.credentialFormat,
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
      throw new NetworkRequestTimeoutException(error.message);
    } else {
      throw new DownloadFailedException(error.message);
    }
  }
}

module.exports = {
  requestCredential,
};
