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

### To run example app 

- **Add issuerMeta, proof, accessToken** in main.js.
- Run the below command in the example folder.
- This will open a brower tab with download VC button.
- If all the issuer meta, proof and accessToken is valid, the VC will be downloaded and displayed in the page itself.
```
npm install && node index.js
```
### npm install error
- If you are facing error related to inji vci client not found in npm registry. This might be because our module might not be published in npm registry at the time you are using it.
- You can try to add the npm package locally by using yarn and try to run the above command again.

### Errors
#### CORS error 
```javascript
Access to XMLHttpRequest at 'https://...' from origin 'https://...' has been blocked by CORS policy: Response to preflight request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```
- CORS error happen when a webpage makes a request to a different domain than the one that served the page, and the server responds with an HTTP error because the “Origin” header in the request is not allowed by the server's CORS configuration.