# INJI VCI Client

The **Inji VCI Client** is a Kotlin-based library built to simplify credential issuance via [OpenID for Verifiable Credential Issuance (OID4VCI)](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-13.html) protocol.
It supports **Issuer Initiated (Credential Offer)** and **Wallet Initiated (Trusted Issuer)** flows, with secure proof handling, PKCE support, and custom error handling.


## 📦 Installation

Add the following dependency to your `build.gradle` to include the library from **Maven Central**:

```groovy
implementation "io.inji:inji-vci-client:0.8.0"
```

## 🏗️ Construction of VCIClient instance

- The `VCIClient` is constructed with a `traceabilityId` which is used to track the session and traceability of the credential request.

```kotlin
val traceabilityId = "sample-trace-id"
val vciClient = VCIClient(traceabilityId)
```

#### Parameters

| Name            | Type   | Required | Default Value | Description                          |
|-----------------|--------|----------|---------------|--------------------------------------|
| traceabilityId  | String | Yes      | N/A           | Unique identifier for the session    |

## 📖 API Overview

### 1. Obtain Issuer Metadata

Retrieve the issuer metadata from the credential issuer's well-known endpoint.

#### Parameters

| Name             | Type   | Required | Default Value | Description                  |
|------------------|--------|----------|---------------|------------------------------|
| credentialIssuer | String | Yes      | N/A           | URI of the Credential Issuer |

#### Returns

`IssuerMetadata` object containing details like `credential_endpoint`, `credential_issuer`, and other IssuerMetadata information from the well-known endpoint of Credential Issuer, which can be used by the consumer to display Issuer information, etc.

> Note: This method does not parse the metadata, it simply returns the raw Network response of well-known endpoint as a `Map<String, Any>`.

#### Example Usage

```kotlin
val issuerMetadata : Map<String, Any> = VCIClient(traceabilityId).getIssuerMetadata(
    credentialIssuer = "https://example.com/issuer"
)

//The response looks similar to this
mapOf(
  "credential_issuer" to "https://example.com/issuer",
  "credential_endpoint" to "https://example.com/issuer/credential",
)
```

### 2. Obtain Credential Configurations Supported

Retrieve credential configurations supported for given issuer from its well-known endpoint.

#### Parameters

| Name             | Type   | Required | Default Value | Description                  |
|------------------|--------|----------|---------------|------------------------------|
| credentialIssuer | String | Yes      | N/A           | URI of the Credential Issuer |

#### Returns
Map of `credential_configurations_supported` objects containing details like `format`, `scope` and other configuration
information from the well-known endpoint of Credential Issuer, which can be used by the consumer to display supported
credential types, etc.

> Note: This method does not parse the metadata, it simply returns the raw Network response of well-known endpoint as a `Map<String, Any>`.

#### Example Usage

```kotlin
val credentialConfigurationsSupported : Map<String, Any> = VCIClient(traceabilityId).getCredentialConfigurationsSupported(
    credentialIssuer = "https://example.com/issuer"
)

//The response looks similar to this
mapOf(
  "credentialConfigId-1" to mapOf(
    "format" to "ldp_vc",
    "credential_definition" to mapOf(
        "type" to listOf("VerifiableCredential", "ExampleCredential")
    )
  ),
  "credentialConfigId-2" to mapOf(
    "format" to "mso_mdoc",
    "doctype" to "org.iso.18013.5.1.mDL"
  ),
  "credentialConfigId-3" to mapOf(
    "format" to "jwt_vc_json",
    "credential_definition" to mapOf(
        "type" to listOf("VerifiableCredential", "UniversityDegreeCredential")
    )
  )
)
```

### 3. Request Credential

### 3.1 Request Credential using Credential Offer

#### fetchCredentialUsingCredentialOffer

- Method: `fetchCredentialUsingCredentialOffer`
- This method allows you to fetch a credential using a credential offer, which can be either an embedded JSON or a URI pointing to the credential offer.
- It supports both **Pre-Authorization** and **Authorization** flows.
- The library handles the PKCE flow internally.
- User-trust based credential download supported through onCheckIssuerTrust callback.
- This method is the recommended way to request credential using credential offer.

##### Parameters

| Name                    | Type                     | Required | Default Value | Description                                                                                                                                                            |
|-------------------------|--------------------------|----------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| credentialOffer         | String                   | Yes      | N/A           | Credential offer as embedded JSON or `credential_offer_uri`                                                                                                            |
| clientMetadata          | ClientMetadata           | Yes      | N/A           | Contains client ID and redirect URI                                                                                                                                    |
| getTxCode               | TxCodeCallback           | No       | N/A           | Optional callback function for TX Code (for Pre-Auth flows)                                                                                                            |
| authorizations          | List<Authorization>      | Yes      | N/A           | Callback functions list to handle authorization and return the resultant authorization response (for Authorization flows) [see authorization details](#authorizations) |
| getTokenResponse        | TokenResponseCallback    | Yes      | N/A           | Callback function to exchange Authorization Grant with Access Token response                                                                                           |
| getProofJwt             | ProofJwtCallback         | Yes      | N/A           | Callback function to prepare proof-jwt for Credential Request                                                                                                          |
| onCheckIssuerTrust      | CheckIssuerTrustCallback | No       | null          | Callback function to get user trust with the Credential Issuer                                                                                                         |
| downloadTimeoutInMillis | Long                     | No       | 10000         | Download timeout set for Credential Request call with Credential Issuer (defaults to 10000 ms)                                                                         |

##### Returns

An instance of `CredentialResponse` containing:

| Name                      | Type        | Description                                                                    |
|---------------------------|-------------|--------------------------------------------------------------------------------|
| credential                | JsonElement | The credential downloaded from the Issuer                                      |
| credentialConfigurationId | String      | The identifier of the respective supported credential from well-known response |
| credentialIssuer          | String      | URI of the Credential Issuer                                                   |

##### Example usage

```kotlin
val credentialResponse: CredentialResponse = vciClient.fetchCredentialUsingCredentialOffer(
  credentialOffer = "openid-credential-offer://?credential_offer_uri=https%3A%2F%2Fsample-issuer.com%2Fcredential-offer",
  clientMetadata = ClientMetadata(clientId = "sample-client-id", redirectUri = "https://sample-wallet.com/callback"),
  getTxCode = object : TxCodeCallback {
    override suspend fun invoke(p1: String?, p2: String?, p3: Int?): String {
      // Handle the transaction code retrieval logic here
      val txCode = "sampleTxCode"
      return txCode
    }
  },
  authorizations = listOf(
      // Presentation During Issuance flow for authorization
      AuthorizationMethod.PresentationDuringIssuance(
          selectCredentialsForPresentation = selectCredentialsForPresentationCallback(),
          signVerifiablePresentation = signVerifiablePresentationCallback(),
          ldpVpSignatureSuite = "Ed25519Signature2020"
      ),
      // Redirect to Web flow for Web view authorization
      AuthorizationMethod.RedirectToWeb(openWebPage = openWebPageCallback())
  ),
  getTokenResponse = object : TokenResponseCallback {
    override suspend fun invoke(tokenRequest: TokenRequest): TokenResponse {
      // Handle the token response retrieval logic here
      //Exchange authorization code for access token
      return TokenResponse(
        accessToken = "sampleAccessToken",
        cNonce = "sampleNonce",
        tokenType = "Bearer",
        expiresIn = 3600,
        cNonceExpiresIn = 3600,
      )
    }
  },
  getProofJwt = object : ProofJwtCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      cNonce: String?,
      proofSigningAlgorithmsSupported: List<String>
    ): String {
      // Prepare payload for JWT
      //Sign the JWT with the private key as per the proofSigningAlgorithmsSupported
      val jwt = "sampleProofJwt"
      return jwt
    }
  },
  onCheckIssuerTrust = object : CheckIssuerTrustCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      issuerDisplay: List<Map<String, Any>>
    ): Boolean {
      // Handle the issuer trust check logic here
      return true // Assume the issuer is trusted for this example
    }
  },
  downloadTimeoutInMillis = 10000
)

//Consider the credential is a Driver's license credential (credential format `mso_mdoc`)
val credentialResponse = vciClient.fetchCredentialUsingCredentialOffer(credentialOffer, clientMetadata, getTxCode, authorizations, getTokenResponse, getProofJwt, onCheckIssuerTrust, downloadTimeoutInMillis)
credentialResponse.credential // This will be a JsonElement containing the credential data. eg - JsonPrimitive("omdk...t")
credentialResponse.credentialConfigurationId // eg - "DriversLicense"
credentialResponse.credentialIssuer // eg - "https://sample-issuer.com"
```

#### requestCredentialByCredentialOffer (deprecated - use `fetchCredentialUsingCredentialOffer` instead)

- Method: `requestCredentialByCredentialOffer`
- This method allows you to request a credential using a credential offer, which can be either an embedded JSON or a URI pointing to the credential offer.
- It supports both **Pre-Authorization** and **Authorization** flows.
- The library handles the PKCE flow internally.
- User-trust based credential download supported through onCheckIssuerTrust callback.

##### Parameters

| Name                    | Type                     | Required | Default Value | Description                                                                                    |
|-------------------------|--------------------------|----------|---------------|------------------------------------------------------------------------------------------------|
| credentialOffer         | String                   | Yes      | N/A           | Credential offer as embedded JSON or `credential_offer_uri`                                    |
| clientMetadata          | ClientMetadata           | Yes      | N/A           | Contains client ID and redirect URI                                                            |
| getTxCode               | TxCodeCallback           | No       | N/A           | Optional callback function for TX Code (for Pre-Auth flows)                                    |
| authorizeUser           | AuthorizeUserCallback    | Yes      | N/A           | Handles authorization and returns the code (for Authorization flows)                           |
| getTokenResponse        | TokenResponseCallback    | Yes      | N/A           | Callback function to exchange Authorization Grant with Access Token response                   |
| getProofJwt             | ProofJwtCallback         | Yes      | N/A           | Callback function to prepare proof-jwt for Credential Request                                  |
| onCheckIssuerTrust      | CheckIssuerTrustCallback | No       | null          | Callback function to get user trust with the Credential Issuer                                 |
| downloadTimeoutInMillis | Long                     | No       | 10000         | Download timeout set for Credential Request call with Credential Issuer (defaults to 10000 ms) |

##### Returns

An instance of `CredentialResponse` containing:

| Name                      | Type        | Description                                                                    |
|---------------------------|-------------|--------------------------------------------------------------------------------|
| credential                | JsonElement | The credential downloaded from the Issuer                                      |
| credentialConfigurationId | String      | The identifier of the respective supported credential from well-known response |
| credentialIssuer          | String      | URI of the Credential Issuer                                                   |

##### Example usage

```kotlin
val credentialResponse: CredentialResponse = vciClient.requestCredentialByCredentialOffer(
  credentialOffer = "openid-credential-offer://?credential_offer_uri=https%3A%2F%2Fsample-issuer.com%2Fcredential-offer",
  clientMetadata = ClientMetadata(clientId = "sample-client-id", redirectUri = "https://sample-wallet.com/callback"),
  getTxCode = object : TxCodeCallback {
    override suspend fun invoke(p1: String?, p2: String?, p3: Int?): String {
      // Handle the transaction code retrieval logic here
      val txCode = "sampleTxCode"
      return txCode
    }
  },
  authorizeUser = object : AuthorizeUserCallback {
    override suspend fun invoke(authEndpoint: String): String {
      // Handle the user authorization logic here
      val authCode = "sampleAuthCode"
      return authCode
    }
  },
  getTokenResponse = object : TokenResponseCallback {
    override suspend fun invoke(tokenRequest: TokenRequest): TokenResponse {
      // Handle the token response retrieval logic here
      //Exchange authorization code for access token
      return TokenResponse(
        accessToken = "sampleAccessToken",
        cNonce = "sampleNonce",
        tokenType = "Bearer",
        expiresIn = 3600,
        cNonceExpiresIn = 3600,
      )
    }
  },
  getProofJwt = object : ProofJwtCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      cNonce: String?,
      proofSigningAlgorithmsSupported: List<String>
    ): String {
      // Prepare payload for JWT
      //Sign the JWT with the private key as per the proofSigningAlgorithmsSupported
      val jwt = "sampleProofJwt"
      return jwt
    }
  },
  onCheckIssuerTrust = object : CheckIssuerTrustCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      issuerDisplay: List<Map<String, Any>>
    ): Boolean {
      // Handle the issuer trust check logic here
      return true // Assume the issuer is trusted for this example
    }
  },
  downloadTimeoutInMillis = 10000
)

//Consider the credential is a Driver's license credential (credential format `mso_mdoc`)
val mdocCredentialResponse = vciClient.requestCredentialByCredentialOffer(credentialOffer, clientMetadata, getTxCode, authorizeUser, getTokenResponse, getProofJwt, onCheckIssuerTrust, downloadTimeoutInMillis)
credentialResponse.credential // This will be a JsonElement containing the credential data. eg - JsonPrimitive("omdk...t")
credentialResponse.credentialConfigurationId // eg - "DriversLicense"
credentialResponse.credentialIssuer // eg - "https://sample-issuer.com"
```

### 3.2 Request Credential from Trusted Issuer

#### fetchCredentialFromTrustedIssuer
- Method: `fetchCredentialFromTrustedIssuer`
- It supports **Authorization** flow.
- The library handles the PKCE flow internally.

#### Parameters

| Name                      | Type                  | Required | Default Value | Description                                                                                                                                                                               |
|---------------------------|-----------------------|----------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| credentialIssuer          | String                | Yes      | N/A           | URI of the Credential Issuer                                                                                                                                                              |
| credentialConfigurationId | String                | Yes      | N/A           | Identifier of the respective supported credential from well-known response                                                                                                                |
| clientMetadata            | ClientMetadata        | Yes      | N/A           | Contains client ID and redirect URI                                                                                                                                                       |
| authorizations            | List<Authorization>   | Yes      | N/A           | Callback functions list to handle authorization and return the resultant authorization response (for Authorization flows) [see authorization details](#authorizations)                     |
| getTokenResponse          | TokenResponseCallback | Yes      | N/A           | Callback function to exchange Authorization Grant with Access Token response                                                                                                              |
| getProofJwt               | ProofJwtCallback      | Yes      | N/A           | Callback function to prepare proof-jwt for Credential Request                                                                                                                             |
| downloadTimeoutInMillis   | Long                  | No       | 10000         | Download timeout set for Credential Request call with Credential Issuer (defaults to 10000 ms)                                                                                            |

#### Returns

An instance of `CredentialResponse` containing:

| Name                      | Type        | Description                                                                    |
|---------------------------|-------------|--------------------------------------------------------------------------------|
| credential                | JsonElement | The credential downloaded from the Issuer                                      |
| credentialConfigurationId | String      | The identifier of the respective supported credential from well-known response |
| credentialIssuer          | String      | URI of the Credential Issuer                                                   |

#### Example usage

```kotlin
val credentialResponse: CredentialResponse = vciClient.fetchCredentialFromTrustedIssuer(
  credentialIssuer = "https://sample-issuer.com",
  credentialConfigurationId = "DriversLicense",
  clientMetadata = ClientMetadata(
    clientId = "sample-client-id",
    redirectUri = "https://sample-wallet.com/callback"
  ),
  authorizations = listOf(
    // Presentation During Issuance flow for authorization
    AuthorizationMethod.PresentationDuringIssuance(
        selectCredentialsForPresentation = selectCredentialsForPresentationCallback(),
        signVerifiablePresentation = signVerifiablePresentationCallback(),
        ldpVpSignatureSuite = "Ed25519Signature2020"
    ),
    // Redirect to Web flow for Web view authorization
    AuthorizationMethod.RedirectToWeb(openWebPage = openWebPageCallback())
  ),
  getTokenResponse = object : TokenResponseCallback {
    override suspend fun invoke(tokenRequest: TokenRequest): TokenResponse {
      // Handle the token response retrieval logic here
      //Exchange authorization code for access token
      return TokenResponse(
        accessToken = "sampleAccessToken",
        cNonce = "sampleNonce",
        tokenType = "Bearer",
        expiresIn = 3600,
        cNonceExpiresIn = 3600,
      )
    }
  },
  getProofJwt = object : ProofJwtCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      cNonce: String?,
      proofSigningAlgorithmsSupported: List<String>
    ): String {
      // Prepare payload for JWT
      //Sign the JWT with the private key as per the proofSigningAlgorithmsSupported
      val jwt = "sampleProofJwt"
      return jwt
    }
  },
  downloadTimeoutInMillis = 10000
)

//Consider the credential is a Driver's license credential (credential format `mso_mdoc`)
val mdocCredentialResponse = vciClient.fetchCredentialFromTrustedIssuer(credentialIssuer, credentialConfigurationId, clientMetadata, authorizations, getTokenResponse, getProofJwt, downloadTimeoutInMillis)
credentialResponse.credential // This will be a JsonElement containing the credential data. eg - JsonPrimitive("omdk...t")
credentialResponse.credentialConfigurationId // eg - "DriversLicense"
credentialResponse.credentialIssuer // eg - "https://sample-issuer.com"
```

#### requestCredentialFromTrustedIssuer (deprecated - use `fetchCredentialFromTrustedIssuer` instead)
- Method: `requestCredentialFromTrustedIssuer`
- This method allows you to request a credential from a trusted issuer of Wallet.
- It supports **Authorization** flow.
- The library handles the PKCE flow internally.

#### Parameters

| Name                      | Type                  | Required | Default Value | Description                                                                                    |
|---------------------------|-----------------------|----------|---------------|------------------------------------------------------------------------------------------------|
| credentialIssuer          | String                | Yes      | N/A           | URI of the Credential Issuer                                                                   |
| credentialConfigurationId | String                | Yes      | N/A           | Identifier of the respective supported credential from well-known response                     |
| clientMetadata            | ClientMetadata        | Yes      | N/A           | Contains client ID and redirect URI                                                            |
| authorizeUser             | AuthorizeUserCallback | Yes      | N/A           | Handles authorization and returns the code (for Authorization flows)                           |
| getTokenResponse          | TokenResponseCallback | Yes      | N/A           | Callback function to exchange Authorization Grant with Access Token response                   |
| getProofJwt               | ProofJwtCallback      | Yes      | N/A           | Callback function to prepare proof-jwt for Credential Request                                  |
| downloadTimeoutInMillis   | Long                  | No       | 10000         | Download timeout set for Credential Request call with Credential Issuer (defaults to 10000 ms) |

#### Returns

An instance of `CredentialResponse` containing:

| Name                      | Type        | Description                                                                    |
|---------------------------|-------------|--------------------------------------------------------------------------------|
| credential                | JsonElement | The credential downloaded from the Issuer                                      |
| credentialConfigurationId | String      | The identifier of the respective supported credential from well-known response |
| credentialIssuer          | String      | URI of the Credential Issuer                                                   |

#### Example usage

```kotlin
val credentialResponse: CredentialResponse = vciClient.requestCredentialFromTrustedIssuer(
  credentialIssuer = "https://sample-issuer.com",
  credentialConfigurationId = "DriversLicense",
  clientMetadata = ClientMetadata(
    clientId = "sample-client-id",
    redirectUri = "https://sample-wallet.com/callback"
  ),
  authorizeUser = object : AuthorizeUserCallback {
    override suspend fun invoke(authEndpoint: String): String {
      // Handle the user authorization logic here
      val authCode = "sampleAuthCode"
      return authCode
    }
  },
  getTokenResponse = object : TokenResponseCallback {
    override suspend fun invoke(tokenRequest: TokenRequest): TokenResponse {
      // Handle the token response retrieval logic here
      //Exchange authorization code for access token
      return TokenResponse(
        accessToken = "sampleAccessToken",
        cNonce = "sampleNonce",
        tokenType = "Bearer",
        expiresIn = 3600,
        cNonceExpiresIn = 3600,
      )
    }
  },
  getProofJwt = object : ProofJwtCallback {
    override suspend fun invoke(
      credentialIssuer: String,
      cNonce: String?,
      proofSigningAlgorithmsSupported: List<String>
    ): String {
      // Prepare payload for JWT
      //Sign the JWT with the private key as per the proofSigningAlgorithmsSupported
      val jwt = "sampleProofJwt"
      return jwt
    }
  },
  downloadTimeoutInMillis = 10000
)

//Consider the credential is a Driver's license credential (credential format `mso_mdoc`)
val mdocCredentialResponse = vciClient.requestCredentialFromTrustedIssuer(credentialIssuer, credentialConfigurationId, clientMetadata, authorizeUser, getTokenResponse, getProofJwt, downloadTimeoutInMillis)
credentialResponse.credential // This will be a JsonElement containing the credential data. eg - JsonPrimitive("omdk...t")
credentialResponse.credentialConfigurationId // eg - "DriversLicense"
credentialResponse.credentialIssuer // eg - "https://sample-issuer.com"
```

##### Authorizations
The `authorizations` parameter is a list of `Authorization` objects indicating the supported authorizations of the Wallet for the download flow. Currently, library supports two authorization flows - _Redirect To Web_ and _Presentation During Issuance_. Library exposes the supported authorization flows via class - `AuthorizationMethod`

1. Redirect To Web (for Authorization flow)

Redirect the user to the authorization endpoint (authorization server) in a web view or browser, and get the authorization response parameters back after successful authorization.

**Parameters :**

| Name        | Type                | Required | Default Value | Description                                                                                                                                                                         |
|-------------|---------------------|----------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| openWebPage | OpenWebPageCallback | Yes      | N/A           | Callback function to open the authorization endpoint in a web view or browser, and return the authorization response parameters (e.g., code, state) after successful authorization. |

**Example usage**
```kotlin
AuthorizationMethod.RedirectToWeb(
  openWebPage = object : OpenWebPageCallback {
    override suspend fun invoke(authorizationEndpoint: String): Map<String, String> {
        // Handle the user authorization logic here
        // Open a web view or browser with the authorizationEndpoint
        // Return the authorization response parameters (e.g., code, state)
        val result: Map<String, String> = openWebViewAndGetResult(authorizationEndpoint)
        return result
    }
  }
)
```
> Note: The Redirect to Web flow for an interactive authorization flow is exposed as an experimental API, and is expected to be improved in future releases.

2. Presentation During Issuance

Presentation During Issuance flow allows the Wallet to present a verifiable presentation to the Credential Issuer during the credential download process, which can be used by the issuer to verify certain claims about the user before issuing the credential. The authorization for the download here is presentation of another credential (or a verifiable presentation) instead of user interaction-based authorization as in Redirect To Web flow.

###### Specification Reference

This implementation follows - [OpenID4VCI v1.1 Specification Commit](https://github.com/openid/OpenID4VCI/blob/31636e9bb7f0eef6933175e1e41c78ce79a69783/1.1/openid-4-verifiable-credential-issuance-1_1.md)

> Note:
> - While this library primarily implements OpenID4VCI draft 13 and 11, the Presentation During Issuance feature follows the v1.1 specification as mentioned above.
> - For Presentation During Issuance flow, this VCI client library internally uses [inji-openid4vp](https://github.com/inji/inji-openid4vp/tree/master/kotlin) library to construct the VP and handle the presentation exchange with the issuer.

**Parameters :**

| Name                             | Type                                     | Required | Default Value | Description                                                                                                                                                                                                                                                                                                                                                                                            |
|----------------------------------|------------------------------------------|----------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| selectCredentialsForPresentation | SelectCredentialsForPresentationCallback | Yes      | N/A           | Callback function to select credentials from the wallet to be presented to the issuer during issuance as per the Issuer's request. The callback will be invoked with a VP request, this VP request will be used by the Wallet to ask the user for selecting the credentials and then the selected credentials are returned back                                                                        |
| signVerifiablePresentation       | SignVerifiablePresentationCallback       | Yes      | N/A           | Callback function to sign the data which will be used for Verifiable Presentation construction. The callback will be invoked with the data to be signed, and the wallet needs to sign this data with the appropriate key and return the signature back to the library.                                                                                                                                 |
| ldpVpSignatureSuite              | String                                   | No       | null          | The signature suite to be used for signing the VP in case of LDP VCs. It is mandatory to provide this parameter if the credential being requested is of format `ldp_vc`. The library will use this information to prepare the proof for the Verifiable Presentation accordingly. Supported values are - `Ed25519Signature2020`, `Ed25519Signature2018`, `JsonWebSignature2020` and `RSASignature2018`. |


**Example usage**

```kotlin
AuthorizationMethod.PresentationDuringIssuance(
                            selectCredentialsForPresentation = object : SelectCredentialsForPresentationCallback {
                                    override suspend fun invoke(
                                        presentationRequest: AuthorizationRequest
                                    ): Map<String, Map<FormatType, List<Any>>> {
                                        // Handle the logic to select credentials from the wallet as per the presentation request
                                        // Handle the logic for obtaining consent from the user for presenting the credentials to the issuer
                                        val selectedCredentials: Map<String, Map<FormatType, List<Any>>> = selectCredentials(presentationRequest)
                                        return selectedCredentials
                                    }
                                },
                            signVerifiablePresentation = object : SignVerifiablePresentationCallback {
                                    override suspend fun invoke(
                                        payload: List<UnsignedVPTokenV2>
                                    ): List<VPTokenSigningResultV2> {
                                        // Handle the logic to sign the data with the appropriate key as per the credential descriptor and signature suite
                                        // From UnsignedVPTokenV2 use the data like format, holderKeyReference and signatureAlgorithm to identify the key to be used for signing and the algorithm to be used for signing the dataToSign, and then return the signature result to the library
                                        val signedData : List<VPTokenSigningResultV2> = signDataForVP(payload)
                                        // since the payload is a list of data to be signed for each credential, the result is also a list containing the signature result for each credential, and the library will take care of constructing the VP with the respective proof for each credential accordingly
                                        // To avoid any confusion, the library will expect the implementation of this callback to return a list of signature results corresponding to each credential in the same order as the payload, and the library will match the signature result with the respective credential based on the order of the payload list.
                                        return  signedData
                                    }
                            },
                            ldpVpSignatureSuite = "Ed25519Signature2020"
                    )
```

[//]: # (The branch in inji-wallet for pdi docs link is pointed to master intentionally to ensure that the latest documentation is always referred.)
> For more details on the Presentation During Issuance flow and the expected implementation of the callbacks, please refer to the [inji-wallet Presentation During Issuance documentation](https://github.com/inji/inji-wallet/blob/master/docs/presentation-during-issuance-support.md)


### 3.3 Request Credential
- Method: `requestCredential`
- Request for credential from the providers (credential issuer), and receive the credential back.

> Note: This method is deprecated and will be removed in future releases. Please migrate to [`fetchCredentialUsingCredentialOffer()`](#fetchcredentialusingcredentialoffer) or [`fetchCredentialFromTrustedIssuer()`](#fetchcredentialfromtrustedissuer).

#### Parameters

| Name           | Type           | Required | Default Value | Description                                                                |
|----------------|----------------|----------|---------------|----------------------------------------------------------------------------|
| issuerMetadata | IssuerMetaData | Yes      | N/A           | Data object of the issuer details                                          |
| proofJwt       | Proof          | Yes      | N/A           | The proof used for making credential request. Supported proof types : JWT. |
| accessToken    | String         | Yes      | N/A           | token issued by providers based on auth code                               |

##### Construction of issuerMetadata

1. Format: `ldp_vc`
```
val issuerMetadata = IssuerMetaData(
                        CREDENTIAL_AUDIENCE,
                        CREDENTIAL_ENDPOINT, 
                        DOWNLOAD_TIMEOUT, 
                        CREDENTIAL_TYPE, 
                        CredentialFormat.LDP_VC )
```
2. Format: `mso_mdoc`
```
val issuerMetadata = IssuerMetaData(
                        CREDENTIAL_AUDIENCE,
                        CREDENTIAL_ENDPOINT, 
                        DOWNLOAD_TIMEOUT, 
                        DOC_TYPE,
                        CLAIMS,
                        CredentialFormat.MSO_MDOC )
```

> ⚠️ **Note**: The `claims` parameter is optional in the OID4VCI specification for `mso_mdoc` format. While `claims` can be provided in `IssuerMetaData`, this implementation does not include `claims` in the credential request body for `mso_mdoc` format.

3. Format: `vc+sd-jwt`
```
val issuerMetadata = IssuerMetaData(
                        CREDENTIAL_AUDIENCE,
                        CREDENTIAL_ENDPOINT,
                        DOWNLOAD_TIMEOUT,
                        VCT,
                        CredentialFormat.VC_SD_JWT )
```

4. Format: `dc+sd-jwt`
```
val issuerMetadata = IssuerMetaData(
                        CREDENTIAL_AUDIENCE,
                        CREDENTIAL_ENDPOINT,
                        DOWNLOAD_TIMEOUT,
                        VCT,
                        CredentialFormat.DC_SD_JWT )
```
#### Returns

An instance of `CredentialResponse` containing:

| Name                      | Type        | Description                               |
|---------------------------|-------------|-------------------------------------------|
| credential                | JsonElement | The credential downloaded from the Issuer |
| credentialConfigurationId | Null        | N/A                                       |
| credentialIssuer          | Null        | N/A                                       |

##### Sample returned response

```kotlin
val credentialResponse = vciClient.requestCredential(
    issuerMetaData = IssuerMetaData(
                        CREDENTIAL_AUDIENCE,
                        CREDENTIAL_ENDPOINT, 
                        DOWNLOAD_TIMEOUT, 
                        DOC_TYPE,
                        CLAIMS,
                        CredentialFormat.MSO_MDOC ),
    proofJwt = JWTProof(jwtValue = "sampleProofJwt"),
    accessToken = "sampleAccessToken"
)
credentialResponse.credential // This will be a JsonElement containing the credential data. eg - JsonPrimitive("omdk...t")
credentialResponse.credentialConfigurationId // This will be null
credentialResponse.credentialIssuer // This will be null
```

---

## 🚨 Deprecation Notice

The following methods are deprecated and will be removed in future releases. Please migrate to the suggested alternatives.

| Method Name                        | Description                                                                                                                                                              | Deprecated Since | Suggested Alternative                                                                                                                                    |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| requestCredentialFromTrustedIssuer | Request for the download of Verifiable Credential through trusted flow has been improvised to accept different authorizations (web / presentation during issuance)       | 0.7.0            | [fetchCredentialFromTrustedIssuer](#fetchcredentialfromtrustedissuer)                                                                                    |
| requestCredentialByCredentialOffer | Request for download of Verifiable Credential through Credential Offer flow has been improvised to accept different authorizations (web / presentation during issuance). | 0.7.0            | [fetchCredentialUsingCredentialOffer](#fetchcredentialusingcredentialoffer)                                                                              |
| requestCredential                  | Request for credential from the providers (credential issuer), and receive the credential back.                                                                          | 0.4.0            | [fetchCredentialUsingCredentialOffer()](#fetchcredentialusingcredentialoffer) or [fetchCredentialFromTrustedIssuer()](#fetchcredentialfromtrustedissuer) |

---

## 🔐 Security Support

-  **PKCE (Proof Key for Code Exchange)** handled internally (RFC 7636)
-  Supports `S256` code challenge method
-  Secure `c_nonce` binding via proof JWTs

---

## 🛑 Error Handling

All exceptions thrown by the library are subclasses of `VCIClientException`.  
They carry structured error codes like `VCI-001`, `VCI-002` etc., to help consumers identify and recover from failures.

| Code    | Exception Type                          | Description                                                                                              |
|---------|-----------------------------------------|----------------------------------------------------------------------------------------------------------|
| VCI-001 | `AuthorizationServerDiscoveryException` | Failed to discover authorization server                                                                  |
| VCI-002 | `DownloadFailedException`               | Failed to download Credential issuer                                                                     |
| VCI-003 | `InvalidAccessTokenException`           | Access token is invalid                                                                                  |
| VCI-004 | `InvalidDataProvidedException`          | Required details not provided                                                                            |
| VCI-005 | `InvalidPublicKeyException`             | Invalid public key passed metadata                                                                       |
| VCI-006 | `NetworkRequestFailedException`         | Network request failed                                                                                   |
| VCI-007 | `NetworkRequestTimeoutException`        | Network request timed-out                                                                                |
| VCI-008 | `OfferFetchFailedException`             | Failed  to fetch credentialOffer                                                                         |
| VCI-009 | `IssuerMetadataFetchException`          | Failed to fetch issuerMetadata                                                                           |
| VCI-011 | `InteractiveAuthorizationException`     | Failed to perform Interactive authorization (Presentation During Issuance / Redirect to Web interaction) |


---

## 🧪 Testing

Mock-based tests are available covering:

- Credential download flow (offer + trusted issuer)
- Proof JWT signing callbacks
- Token exchange logic

> See `VCIClientTest` for full coverage

## Platform Support

- **Kotlin:** 1.9+
- **JVM:** Java 17
- **Android:** minSdk 23, compileSdk 34
- **Gradle:** 8.0+
- **AGP (Android Gradle Plugin):** 8.0+

Architecture decisions are noted as ADRs [here](../doc/adr).