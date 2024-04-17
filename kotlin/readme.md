## INJI VCI Client

This library takes care of downloading a VC (Verifiable Credential) given the consumer application performs the authorization flow and has its own keypair generation and signing logic

## Usage

### Download VC
```
val credentialResponse: CredentialResponse? = VCIClient().requestCredential(
                        issuer = IssuerMeta(
                            Constants.CREDENTIAL_AUDIENCE,
                            Constants.CREDENTIAL_ENDPOINT,
                            Constants.DOWNLOAD_TIMEOUT,
                            Constants.CREDENTIAL_TYPE,
                            Constants.CREDENTIAL_FORMAT
                        ),
                        signer = ::signer,
                        accessToken = accessToken,
                        publicKeyPem = publicKeyInPem
                    )
```

###### Exceptions

1. InvalidAccessTokenException is thrown when token passed by user is invalid
2. NetworkRequestTimeoutException is thrown when the call times out more than the passed download timeout
3. DownloadFailedException is thrown for any other error scenarios


#### How to use it?

example app can be referenced for more details on how to use it