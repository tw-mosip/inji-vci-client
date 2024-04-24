## INJI VCI Client

This kotlin library takes care of downloading a VC (Verifiable Credential) given the consumer application performs the authorization flow and has its own keypair generation and signing logic

## Installation

TODO: add installation guide

## API

| Method                                                             | ReturnType         | Description                                                                                       |
|--------------------------------------------------------------------|--------------------|---------------------------------------------------------------------------------------------------|
| requestCredential(issuerMeta, ::signer, accessToken, publicKeyPem) | CredentialResponse | Request Credential - Initiates a download request to the credential endpoint added in issuer meta |


### Request Credential

Returns the credential downloaded from the provided issuer.

```
val credentialResponse: CredentialResponse? = VCIClient().requestCredential(
                        IssuerMeta( CREDENTIAL_AUDIENCE, CREDENTIAL_ENDPOINT, DOWNLOAD_TIMEOUT, CREDENTIAL_TYPE, CREDENTIAL_FORMAT ),
                        ::signer,
                        accessToken,
                        publicKeyPem
                    )
```
**Parameters**

| Name         | Type                               | Description                                                                                                  | Sample                                                                                                  |
|--------------|------------------------------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| issuerMeta   | IssuerMeta                         | Data object of the issuer details                                                                            | `IssuerMeta(credentialAudience, credentialEndpoint, downloadTimeout, credentialType, credentialFormat)` |
| signer       | Function  (ByteArray) -> ByteArray | Function which is called to get the signature passing the preHash (Base64EncodedHeader.Base64EncodedPayload) | `fun signer(preHash: ByteArray): ByteArray {//Signing logic}`                                           |
| accessToken  | String                             |                                                                                                              |                                                                                                         |
| publicKeyPem | String                             | Public key in PEM format is passed from the keypair generated                                                |                                                                                                         |



###### Exceptions

1. InvalidAccessTokenException is thrown when token passed by user is invalid
2. NetworkRequestTimeoutException is thrown when the call times out more than the passed download timeout
3. DownloadFailedException is thrown for any other error scenarios


#### More details

An example app is added under /example folder which can be referenced for more details
