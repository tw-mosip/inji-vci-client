import { DownloadFailedException } from "../src/exception/DownloadFailedException";
import { NetworkRequestTimeoutException } from "../src/exception/NetworkRequestTimeoutException";
import { requestCredential } from "../src/VCIClient";
import nock from "nock";

describe("VCI Client test", () => {
  const mockCredentialResponse = {
    format: "ldp_vc",
    credential: {
      issuanceDate: "2024-04-14T16:04:35.304Z",
      credentialSubject: {
        face: "data:image/jpeg;base64,/9j/goKCyuig",
        dateOfBirth: "2000/01/01",
        id: "did:jwk:eyJr80435=",
        UIN: "9012378996",
        email: "mockuser@gmail.com",
      },
      id: "https://domain.net/credentials/12345-87435",
      proof: {
        type: "RsaSignature2018",
        created: "2024-04-14T16:04:35Z",
        proofPurpose: "assertionMethod",
        verificationMethod: "https://domain.net/.well-known/public-key.json",
        jws: "eyJhbGciOiJSUzI11E_d-uifyj84U3XU8TA",
      },
      type: ["VerifiableCredential"],
      "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://domain.net/.well-known/context.json",
        {
          sec: "https://w3id.org/security#",
        },
      ],
      issuer: "https://domain.net/.well-known/issuer.json",
    },
  };
  const credentialEndpoint = "https://domain.net/credential";
  const credentialAudience = "https://domain.net";
  const downloadTimeoutInMillSeconds = 3000;
  const credentialType = ["VerifiableCredential"];
  const credentialFormat = "ldp_vc";
  const proof = "eyJhbdFQwRnFWZjIcQku5nL1E_d-uifyj84U3XU8TA";
  const accessToken = "eyJrahyw5Q_oeX7jXlHffwD2eBo3g";
  const issuerMeta = {
    credentialAudience,
    credentialEndpoint,
    downloadTimeoutInMillSeconds,
    credentialType,
    credentialFormat,
  };

  it("should make api call to credential endpoint with the right params in case of ldpVc", async () => {
    nock("https://domain.net").post("/credential").reply(200, mockCredentialResponse);

    const resp = await requestCredential(issuerMeta, proof, accessToken);

    expect(resp).toEqual(mockCredentialResponse);
  });

  it("should return null when credential endpoint responded with empty body", async () => {
    nock("https://domain.net").post("/credential").reply(200, "");

    const resp = await requestCredential(issuerMeta, proof, accessToken);

    expect(resp).toEqual(null);
  });

  it("should throw download failure exception when credential endpoint response is not 200", async () => {

    try {
      nock("https://domain.net").post("/credential").reply(500, new DownloadFailedException(""));

      await requestCredential(issuerMeta, proof, accessToken);

    } catch (error) {
      expect(error).toBeInstanceOf(DownloadFailedException);
      expect(error.message).toBe("Download failed - Request failed with status code 500");
    }
  });

  it("should throw network request timeout failure exception when timeout is reached", async () => {

    try {
      nock("https://domain.net").post("/credential").delay(4000).reply(200, new NetworkRequestTimeoutException(""));

      await requestCredential(issuerMeta, proof, accessToken);

    } catch (error) {
      expect(error).toBeInstanceOf(NetworkRequestTimeoutException);
      expect(error.message).toBe( "Download failure occurred due to Network request timeout, details -  timeout of 3000ms exceeded");
    }
  });
});