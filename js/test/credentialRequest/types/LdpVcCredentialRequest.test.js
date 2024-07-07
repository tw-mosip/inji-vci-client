import { LdpVcCredentialRequest } from "../../../src/credentialRequest/types/LdpVcCredentialRquest";

test("should create JSON in expected format", () => {
  const credentialEndpoint = "https://domain.net/credential";
  const credentialAudience = "https://domain.net";
  const downloadTimeoutInMillSeconds = 3000;
  const credentialType = ["VerifiableCredential"];
  const proof ="eyJhbGciOiJSUz9mK2p3dCJ9";
  const credentialFormat = "ldp_vc";
  const issuerMeta = {
    credentialAudience,
    credentialEndpoint,
    downloadTimeoutInMillSeconds,
    credentialType,
    credentialFormat,
  };

  const expectedRequestHeader = {
    Authorization: "Bearer accessToken",
    "Content-Type": "application/json",
  };
  const expectedRequestBody =
    '{"format":"ldp_vc","credential_definition":{"type":["VerifiableCredential"],"@context":["https://www.w3.org/2018/credentials/v1"]},"proof":{"proof_type":"jwt","jwt":"eyJhbGciOiJSUz9mK2p3dCJ9"}}';
  

  const ldpVcRequest = new LdpVcCredentialRequest(
    "accessToken",
    issuerMeta,
    proof
  ).constructRequest();

  expect(ldpVcRequest.requestHeader).toEqual(expectedRequestHeader);
  expect(ldpVcRequest.requestBody).toEqual(expectedRequestBody);
});
