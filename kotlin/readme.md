# INJI VCI Client

## Features

- Request credential to Credential issuer

## Installation

TODO: add installation guide

## APIs

##### Request Credential

Initiates a credential request to the credential issuer and returns back the credential.

```
val credentialResponse: CredentialResponse? = VCIClient().requestCredential(
                        IssuerMeta( CREDENTIAL_AUDIENCE, CREDENTIAL_ENDPOINT, DOWNLOAD_TIMEOUT, CREDENTIAL_TYPE, CREDENTIAL_FORMAT ),
                        ::signer,
                        accessToken,
                        publicKeyPem
                    )
```
###### Parameters

| Name         | Type                               | Description                                                                                                  | Sample                                                                                                  |
|--------------|------------------------------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| issuerMeta   | IssuerMeta                         | Data object of the issuer details                                                                            | `IssuerMeta(credentialAudience, credentialEndpoint, downloadTimeout, credentialType, credentialFormat)` |
| signer       | Function  (ByteArray) -> ByteArray | Function which is called to get the signature passing the preHash (Base64EncodedHeader.Base64EncodedPayload) | `fun signer(preHash: ByteArray): ByteArray {//Signing logic}`                                           |
| accessToken  | String                             | Token provided by the credential issuer post authorization and token request                                 |                                                                                                         |
| publicKeyPem | String                             | Public key in PEM format is passed from the keypair generated                                                |                                                                                                         |



###### Exceptions

1. InvalidAccessTokenException is thrown when token passed by user is invalid
2. NetworkRequestTimeoutException is thrown when the call times out more than the passed download timeout
3. DownloadFailedException is thrown when the credential issuer did not respond with credential response


## More details

An example app is added under /example folder which can be referenced for more details
