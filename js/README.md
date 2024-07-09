# INJI VCI Client

Contains OpenId4VCI specification related source code and documentation

## Features

- Request credential to Credential issuer

## API
### requestCredential
- The requestCredential function is used to request a credential from a credential issuer.

### Parameters

- **issuerMeta (Object):** Metadata related to the issuer. This object should contain the credential format and other necessary issuer information.
- **proof (string):**  A proof string required for the credential request.
- **accessToken (string):** An access token for authorization.

