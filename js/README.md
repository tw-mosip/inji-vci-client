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

## Usage
- To install the dependencies run,
```npm i``` in the root folder.

## Example

```javascript
const {requestCredential} = require("inji-vci-client");

  const credentialEndpoint = "https://example.net/credential";
  const credentialAudience = "https://example.net";
  const downloadTimeoutInMillSeconds = 3000;
  const credentialType = ["<>"];
  const credentialFormat = "<>";
  
  const proof = "<>";
  const accessToken = "<>";
  
  const issuerMetaData = {
    credentialAudience,
    credentialEndpoint,
    downloadTimeoutInMillSeconds,
    credentialType,
    credentialFormat,
  };

const response = await requestCredential(issuerMetaData, proof, accessToken);
// returns the credential response based on the format provided.
```
## Exceptions 
### NetworkRequestTimeoutException

- Throws NetworkRequestTimeoutException when the request operation took more time than the download timeout mentioned in the issuerMetaData.

### DownloadFailedException

- Throws DownloadFailedException when the request operation failed due to unexpected problem occurred.
