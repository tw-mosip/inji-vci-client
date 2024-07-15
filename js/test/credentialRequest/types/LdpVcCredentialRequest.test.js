const LdpVcCredentialRequest = require("../../../src/credentialRequest/types/LdpVcCredentialRequest");
const JWTProof = require("../../../src/proof/jwt/JWTProof");
const IssuerMetaData = require('../../../src/dto/issuerMetaData');

test("should create JSON in expected format", () => {
  const credentialEndpoint = "https://domain.net/credential";
  const credentialAudience = "https://domain.net";
  const downloadTimeoutInMillSeconds = 3000;
  const credentialType = ["VerifiableCredential"];
  const proof = new JWTProof("eyJhbdFQwRnFWZjIcQku5nL1E_d-uifyj84U3XU8TA");
  const credentialFormat = "ldp_vc";
  const issuerMetaData = new IssuerMetaData(
    credentialAudience,
    credentialEndpoint,
    downloadTimeoutInMillSeconds,
    credentialType,
    credentialFormat,
  );

  const expectedRequestHeader = {
    Authorization: "Bearer accessToken",
    "Content-Type": "application/json",
  };
  const expectedRequestBody =
    '{"format":"ldp_vc","credential_definition":{"type":["VerifiableCredential"],"@context":["https://www.w3.org/2018/credentials/v1"]},"proof":{"proof_type":"jwt","jwt":"eyJhbdFQwRnFWZjIcQku5nL1E_d-uifyj84U3XU8TA"}}';

  const ldpVcRequest = new LdpVcCredentialRequest(
    "accessToken",
    issuerMetaData,
    proof
  ).constructRequest();

  expect(ldpVcRequest.requestHeader).toEqual(expectedRequestHeader);
  expect(ldpVcRequest.requestBody).toEqual(expectedRequestBody);
});
