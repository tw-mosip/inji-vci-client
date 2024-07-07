import {CredentialRequestFactory} from './credentialRequest/credentialRequestFactory';
import {DownloadFailedException} from './exception/DownloadFailedException';
import {CredentialResponseFactory} from './credentialResponse/CredentialResponseFactory';
import {NetworkRequestTimeoutException} from './exception/NetworkRequestTimeoutException';
import {NETWORK_TIMEOUT, EMPTY_RESPONSE} from './constants/constants';
import {Logger} from './common/Logger';
import axios from 'axios';

/**
 * @param {Object} issuerMeta
 * @param {string} proof
 * @param {string} accessToken
 */

export async function requestCredential(issuerMeta, proof, accessToken) {
  const request = CredentialRequestFactory.createCredentialRequest(
    issuerMeta.credentialFormat,
    accessToken,
    issuerMeta,
    proof,
  );

  try {
    const response = await axios.post(
      issuerMeta.credentialEndpoint,
      request.requestBody,
      {
        timeout: issuerMeta.downloadTimeoutInMillSeconds,
        headers: request.requestHeader,
      },
    );

    if (response.data !== EMPTY_RESPONSE) {
      const responseBody = JSON.stringify(response.data);
      return CredentialResponseFactory.createCredentialResponse(
        issuerMeta.credentialFormat,
        responseBody,
      );
    } else {
      Logger.warn(
        'The response body from credentialEndpoint is empty, returning null',
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