# INJI VCI Client

## Features

- Request credential to Credential issuer

## Installation

Snapshot builds are available [here](https://oss.sonatype.org/content/repositories/snapshots/io/mosip/inji-vci-client/).

```
implementation "io.mosip:inji-vci-client:1.2-SNAPSHOT"
```

## APIs

##### Request Credential

Request for credential from the providers (credential issuer), and receive the credential back.

```
val credentialResponse: CredentialResponse? = VCIClient().requestCredential(
                        IssuerMetaData( CREDENTIAL_AUDIENCE, CREDENTIAL_ENDPOINT, DOWNLOAD_TIMEOUT, CREDENTIAL_TYPE, CREDENTIAL_FORMAT ),
                        proof,
                        accessToken
                    )
```
###### Parameters

| Name           | Type            | Description                                                                | Sample                                                                                                       |
|----------------|-----------------|----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| issuerMetaData | IssuerMetaData  | Data object of the issuer details                                          | `IssuerMetaData(credentialAudience, credentialEndpoint, downloadTimeout, credentialType, credentialFormat)`  |
| proofJwt       | Proof           | The proof used for making credential request. Supported proof types : JWT. | `JWTProof(jwtValue)`                                                                                         |                                                                                                         |
| accessToken    | String          | token issued by providers based on auth code                               | ""                                                                                                           |



###### Exceptions

1. DownloadFailedException is thrown when the credential issuer did not respond with credential response
2. NetworkRequestTimeoutException is thrown when the request is timedout

## More details

An example app is added under /example folder which can be referenced for more details
